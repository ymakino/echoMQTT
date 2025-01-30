#!/bin/sh

VMNAME=build-paho
VERSION=1.2.5

lxc launch ubuntu:22.04 ${VMNAME} || exit 1

lxc exec ${VMNAME} -- cloud-init status --wait || exit 1

lxc exec ${VMNAME} -- apt-get update || exit 1
lxc exec ${VMNAME} -- apt-get install -y openjdk-8-jdk maven || exit 1

lxc file push build_paho.sh ${VMNAME}/root/ || exit 1
lxc exec ${VMNAME} -- update-java-alternatives -s java-1.8.0-openjdk-amd64 || exit 1
lxc exec ${VMNAME} --env VERSION=${VERSION} -- bash build_paho.sh || exit 1

lxc file pull "${VMNAME}/root/org.eclipse.paho.client.mqttv3-${VERSION}.jar" . || exit 1

lxc stop ${VMNAME} || exit 1
lxc delete ${VMNAME} || exit 1
