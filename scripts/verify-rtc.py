#!/usr/bin/env python3
"""IM-issued tokens -> two real WebRTC clients -> synthetic bidirectional media.

Run with data/rtc-verifier/bin/python. Uses no camera or microphone. Creates
isolated local users and retains the call audit trail; disables these users
on completion. LiveKit and other middleware are preserved.
"""
import asyncio,importlib.util,json,secrets,sys,time
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
import numpy as np
from livekit import rtc
ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('http_local',ROOT/'scripts/smoke-test.py');http=importlib.util.module_from_spec(spec);spec.loader.exec_module(http)
report={'checkedAt':datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),'passed':False,'sdk':'livekit==1.1.21','syntheticMedia':True,'checks':[]}
admin=None;users=[];room_id=None;tokens=[]

def check(label,condition):
    report['checks'].append({'check':label,'passed':bool(condition)});print(('PASS ' if condition else 'FAIL ')+label,flush=True)
    if not condition:raise AssertionError(label)

def request(path,method='GET',data=None,token=None):
    result=http.call('/admin-api/'+path,method,data,token=token)
    if result.get('code')!=0:raise AssertionError(path.split('?')[0]+': '+str(result.get('code'))+' '+str(result.get('msg')))
    return result.get('data')

async def media(url,join_tokens):
    rooms=[rtc.Room(),rtc.Room()];tasks=[];streams=[]
    counts=[{'audioFrames':0,'audibleFrames':0,'videoFrames':0,'matchedVideoFrames':0} for _ in rooms]
    async def consume_audio(index,track):
        stream=rtc.AudioStream(track,sample_rate=48000,num_channels=1);streams.append(stream)
        async for event in stream:
            values=np.frombuffer(event.frame.data,dtype=np.int16).astype(np.float64)
            counts[index]['audioFrames']+=1
            if np.sqrt(np.mean(values*values))>100:counts[index]['audibleFrames']+=1
    async def consume_video(index,track):
        stream=rtc.VideoStream(track,format=rtc.VideoBufferType.RGB24);streams.append(stream)
        async for event in stream:
            frame=event.frame;counts[index]['videoFrames']+=1
            pixels=np.frombuffer(frame.data,dtype=np.uint8).reshape(frame.height,frame.width,3)
            means=pixels.mean(axis=(0,1))
            correct=(means[2]>means[0]+80) if index==0 else (means[0]>means[2]+80)
            if frame.width==160 and frame.height==120 and correct:counts[index]['matchedVideoFrames']+=1
    for index,room in enumerate(rooms):
        def subscribe(track,publication,participant,index=index):
            if track.kind==rtc.TrackKind.KIND_AUDIO:tasks.append(asyncio.create_task(consume_audio(index,track)))
            elif track.kind==rtc.TrackKind.KIND_VIDEO:tasks.append(asyncio.create_task(consume_video(index,track)))
        room.on('track_subscribed',subscribe)
    try:
        # Sequential joins exercise the same server room, with distinct IM identities.
        for room,token in zip(rooms,join_tokens):await room.connect(url,token)
        check('Two clients join LiveKit using IM-issued room tokens',all(r.connection_state==rtc.ConnectionState.CONN_CONNECTED for r in rooms))
        async def publish(index,room):
            audio=rtc.AudioSource(48000,1);video=rtc.VideoSource(160,120)
            audio_track=rtc.LocalAudioTrack.create_audio_track('verification-audio',audio)
            video_track=rtc.LocalVideoTrack.create_video_track('verification-video',video)
            await room.local_participant.publish_track(audio_track,rtc.TrackPublishOptions(source=rtc.TrackSource.SOURCE_MICROPHONE))
            await room.local_participant.publish_track(video_track,rtc.TrackPublishOptions(source=rtc.TrackSource.SOURCE_CAMERA,video_codec=rtc.VideoCodec.VP8,simulcast=False))
            pixels=np.zeros((120,160,3),dtype=np.uint8);pixels[:]=[230,40,20] if index==0 else [20,40,230]
            video_frame=rtc.VideoFrame(160,120,rtc.VideoBufferType.RGB24,pixels.tobytes())
            try:
                for step in range(500):
                    frame=rtc.AudioFrame.create(48000,1,480)
                    positions=(np.arange(480)+step*480)/48000
                    np.frombuffer(frame.data,dtype=np.int16)[:]=(5000*np.sin(2*np.pi*(440+index*220)*positions)).astype(np.int16)
                    await audio.capture_frame(frame)
                    if step%10==0:video.capture_frame(video_frame)
                    await asyncio.sleep(.01)
            finally:
                await audio.aclose();await video.aclose()
        await asyncio.wait_for(asyncio.gather(*(publish(i,r) for i,r in enumerate(rooms))),timeout=25)
        await asyncio.sleep(.3)
        report['mediaReceived']=counts
        for index,stats in enumerate(counts):
            check(f'Client {index+1} receives and decodes actual remote audio samples',stats['audibleFrames']>=30)
            check(f'Client {index+1} receives remote video with the expected color and dimensions',stats['matchedVideoFrames']>=5)
    finally:
        for stream in streams:await stream.aclose()
        for task in tasks:task.cancel()
        if tasks:await asyncio.gather(*tasks,return_exceptions=True)
        for room in rooms:await room.disconnect()

try:
    admin=request('system/auth/login','POST',{'username':'admin','password':'Aicloud@123'})['accessToken']
    suffix=str(time.time_ns())[-12:]
    for index in range(2):
        username='rtc'+str(index)+suffix;password=secrets.token_urlsafe(10)
        user=request('system/user/create','POST',{'username':username,'nickname':'音视频验收'+str(index),'password':password,'postIds':[],'sex':0},admin)
        users.append(user)
        tokens.append(request('system/auth/login','POST',{'username':username,'password':password})['accessToken'])
    report['users']=users
    application=request('im/friend-request/apply','POST',{'toUserId':users[1],'applyContent':'本地媒体联调','addSource':1},tokens[0])
    if application:
        saved=request('im/friend-request/get?id='+str(application),token=tokens[1])
        if saved['handleResult']==0:request('im/friend-request/agree?id='+str(application),'PUT',token=tokens[1])
    call=request('im/rtc/create','POST',{'conversationType':1,'mediaType':2,'inviteeIds':[users[1]]},tokens[0]);room_id=call['room']
    accepted=request('im/rtc/accept?room='+room_id,'POST',token=tokens[1])
    check('IM invite and acceptance persist an active video call',accepted['status']==20 and accepted['room']==room_id)
    report['room']=room_id
    # A third identity must not be allowed to join this private call.
    rejected=http.call('/admin-api/im/rtc/accept?room='+room_id,'POST',token=admin)
    check('Uninvited user cannot obtain a private call token',rejected.get('code') not in [0,None])
    asyncio.run(media(call['livekitUrl'],[call['token'],accepted['token']]))
    check('Media verification completed through the local SFU',True)
    report['passed']=all(c['passed'] for c in report['checks'])
except Exception as error:
    report['error']=str(error);print('FAIL '+str(error),flush=True)
finally:
    if room_id and tokens:
        try:request('im/rtc/leave?room='+room_id,'POST',token=tokens[0])
        except Exception as error:report['callCleanupError']=str(error)
    if admin:
        for user in users:
            try:request('system/user/update-status','PUT',{'id':user,'status':1},admin)
            except Exception as error:report['passed']=False;report['userCleanupError']=str(error)
    (ROOT/'docs/rtc-http-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if report['passed'] else 1)
