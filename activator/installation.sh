#!/bin/sh

sudo su -

apt-get update

# Add swap
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo "\n/swapfile   none    swap    sw    0   0" >> /etc/fstab

# Add tallred to machine
adduser tallred
echo "tallred ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
sudo su tallred
mkdir ~/.ssh
touch ~/.ssh/authorized_keys
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

# Install git
apt-get -y install git
mkdir slhp
git clone https://github.com/tantalim/scala-server.git

# Install Java for sbt builds
apt-get install python-software-properties
add-apt-repository ppa:webupd8team/java
apt-get update
apt-get install oracle-java8-installer

# Install nginx
apt-get -y install nginx
cp scala-server/conf/nginx.conf /etc/nginx/sites-available/tantalim.com
ln -s /etc/nginx/sites-available/tantalim.com /etc/nginx/sites-enabled/tantalim.com
/etc/init.d/nginx restart

# Install Docker
apt-get -y install docker.io
