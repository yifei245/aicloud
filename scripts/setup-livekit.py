#!/usr/bin/env python3
"""Create the user-approved local LiveKit once, preserving containers and credentials."""
import os
import secrets
import socket
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
IMAGE = 'livekit/livekit-server:v1.13.9'
NAME = 'aicloud-livekit'
env_file = ROOT / '.env'
if not env_file.exists():
    raise SystemExit('Configure .env with scripts/init-db.py first.')
lines = env_file.read_text().splitlines()
settings = dict(line.split('=', 1) for line in lines if line and not line.startswith('#') and '=' in line)
for key, default in {
    'AICLOUD_LIVEKIT_API_KEY': 'aicloud-' + secrets.token_hex(8),
    'AICLOUD_LIVEKIT_API_SECRET': secrets.token_urlsafe(48),
    'AICLOUD_LIVEKIT_URL': 'ws://127.0.0.1:7880',
}.items():
    if not settings.get(key):
        settings[key] = default
        lines.append(f'{key}={default}')
env_file.write_text('\n'.join(lines) + '\n')
os.chmod(env_file, 0o600)
config_dir = ROOT / 'data/livekit'
config_dir.mkdir(parents=True, exist_ok=True)
config = config_dir / 'livekit.yaml'
content = f'''port: 7880
rtc:
  tcp_port: 7881
  udp_port: 7882
  use_external_ip: false
  node_ip: 127.0.0.1
keys:
  {settings['AICLOUD_LIVEKIT_API_KEY']}: {settings['AICLOUD_LIVEKIT_API_SECRET']}
webhook:
  api_key: {settings['AICLOUD_LIVEKIT_API_KEY']}
  urls:
    - http://host.docker.internal:48093/admin-api/im/livekit/webhook
'''
existing = subprocess.run(['docker', 'inspect', NAME, '--format', '{{.Config.Image}}'],
                          capture_output=True, text=True)
if existing.returncode == 0:
    if existing.stdout.strip() != IMAGE or not config.exists() or config.read_text() != content:
        raise SystemExit('Existing LiveKit/config differs; preserved it. Reconcile configuration manually.')
    subprocess.run(['docker', 'start', NAME], check=True, stdout=subprocess.DEVNULL)
    print(f'{NAME}: existing container retained and started')
else:
    if subprocess.run(['docker', 'info'], capture_output=True).returncode:
        raise SystemExit('Cannot access the local Docker daemon.')
    for port in (7880, 7881):
        with socket.socket() as probe:
            try:
                probe.bind(('127.0.0.1', port))
            except OSError:
                raise SystemExit(f'Port {port} is occupied; existing service preserved.')
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as probe:
        try:
            probe.bind(('127.0.0.1', 7882))
        except OSError:
            raise SystemExit('UDP port 7882 is occupied; existing service preserved.')
    if config.exists() and config.read_text() != content:
        raise SystemExit('Existing config differs; preserved it.')
    config.write_text(content)
    os.chmod(config, 0o600)
    subprocess.run(['docker', 'run', '-d', '--name', NAME, '--restart', 'unless-stopped',
                    '--memory', '512m', '--cpus', '1',
                    '-p', '127.0.0.1:7880:7880', '-p', '127.0.0.1:7881:7881',
                    '-p', '127.0.0.1:7882:7882/udp',
                    '-v', f'{config}:/etc/livekit.yaml:ro', IMAGE,
                    '--config', '/etc/livekit.yaml', '--bind', '0.0.0.0'],
                   check=True, stdout=subprocess.DEVNULL)
    print(f'{NAME}: created once, retained; ws://127.0.0.1:7880')
