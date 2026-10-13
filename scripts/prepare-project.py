#!/usr/bin/env python3
"""One-time migration repairs; works only on this project's backend files."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
apps = []
for p in ROOT.glob('aicloud-*/**/pom.xml'):
    config = p.parent/'src/main/resources/application.yaml'
    if not config.exists() or not list((p.parent/'src/main/java').rglob('*Application.java')):
        continue
    pom = ET.parse(p).getroot()
    artifact = pom.findtext('m:artifactId', namespaces=ns)
    content = config.read_text()
    name = re.search(r'\n\s+name: ([\w-]+)', content)[1]
    port_match = re.search(r'\nserver:\n\s+port: (\d+)', content)
    if not port_match:
        port_match = re.search(r'\n\s+port: (\d+)', content)
    port = int(port_match[1]) if port_match else 18080
    if name == "iot-gateway-server": port=1883
    # gateway-local explicitly overrides its upstream port.
    if name == 'gateway-server': port = 18080
    apps.append((name, artifact, p.parent.relative_to(ROOT).as_posix(), port))

header = '''name: aicloud
# Only aicloud applications. Existing local middleware remains outside this project.
x-app-env: &app-env
  MYSQL_HOST: host.docker.internal
  MYSQL_PORT: 3306
  MYSQL_USER: ${MYSQL_USER:?set MYSQL_USER in .env}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD:?set MYSQL_PASSWORD in .env}
  REDIS_HOST: host.docker.internal
  REDIS_PORT: 6379
  REDIS_PASSWORD: ${REDIS_PASSWORD:-}
  REDIS_DATABASE: ${REDIS_DATABASE:-12}
  NACOS_ADDR: host.docker.internal:8848
  IOT_RPC_URL: http://iot-server:48098
  NACOS_USERNAME: ${NACOS_USERNAME:-nacos}
  NACOS_PASSWORD: ${NACOS_PASSWORD:-nacos}
  AICLOUD_INTERNAL_SECRET: ${AICLOUD_INTERNAL_SECRET:?set AICLOUD_INTERNAL_SECRET in .env}
  SPRING_PROFILES_ACTIVE: local
services:
'''
out = [header]
inventory = []
for name, artifact, directory, port in sorted(apps):
    if name == 'aicloud-server': continue  # optional core aggregate runs locally
    profile = name.removesuffix('-server')
    profiles = '' if profile in {'gateway','system','infra','member'} else f'    profiles: ["{profile}", "full"]\n'
    out.append(f'''  {name}:
    build:
      context: .
      dockerfile: deploy/Dockerfile
      args:
        JAR_FILE: {directory}/target/{artifact}.jar
    image: aicloud/{name}:1.0.0
{profiles}    env_file: [".env"]
    environment:
      <<: *app-env
    extra_hosts: ["host.docker.internal:host-gateway"]
    restart: on-failure:3
    mem_limit: {"1536m" if profile in {"ai", "bpm"} else "1024m"}
    volumes: ["{profile}-data:/app/data"]
    healthcheck:
      test: ["CMD-SHELL", "{'nc -z 127.0.0.1 1883' if profile == 'iot-gateway' else f'wget -q -O /dev/null http://127.0.0.1:{port}/actuator/health'}"]
      start_period: 120s
      interval: 20s
      timeout: 5s
      retries: 6
''')
    if profile == 'gateway':
        out.append('    ports: ["127.0.0.1:${GATEWAY_PORT:-18080}:18080"]\n')
    if profile == 'iot-gateway':
        out.append('    ports: ["127.0.0.1:${IOT_MQTT_PORT:-1883}:1883"]\n')
    inventory.append({'name':name,'artifact':artifact,'directory':directory,'port':port,'profile':profile})
out.append('volumes:\n')
for app in inventory: out.append(f'  {app["profile"]}-data:\n')
(ROOT/'compose.yaml').write_text(''.join(out))
import json
(ROOT/'docs/services.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n')
print(f'Repaired backend configuration; Compose contains {len(inventory)} independent services.')
