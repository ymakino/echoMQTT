#!/bin/sh

PAHO_PATH=paho.mqtt.java
PAHO_URL=https://github.com/eclipse/paho.mqtt.java.git
VERSION=1.2.5

rm -fr ${PAHO_PATH}

git clone ${PAHO_URL} -b v${VERSION}

if [ $? -ne 0 ]; then
  echo "Cannot clone: ${PAHO_URL}"
  exit 1
fi

sed -i -s 's|\(<module>org.eclipse.paho.client.mqttv3.test</module>\)|<!-- \1 -->|' ${PAHO_PATH}/pom.xml
sed -i -s 's|\(<module>org.eclipse.paho.sample.utility</module>\)|<!-- \1 -->|' ${PAHO_PATH}/pom.xml
sed -i -s 's|\(<module>org.eclipse.paho.mqttv5.client.test</module>\)|<!-- \1 -->|' ${PAHO_PATH}/pom.xml

( cd ${PAHO_PATH} && mvn install )

if [ $? -ne 0 ]; then
  echo "Cannot install: ${PAHO_PATH}"
  exit 1
fi

cp ${PAHO_PATH}/org.eclipse.paho.client.mqttv3/target/org.eclipse.paho.client.mqttv3-${VERSION}.jar . && rm -fr ${PAHO_PATH}


