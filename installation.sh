#!/bin/sh

# Add tallred to machine
adduser tallred
sudo su tallred
mkdir ~/.ssh
touch ~/.ssh/authorized_keys
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
echo "\ntallred ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers

# Add swap
fallocate -l 1G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo "\n/swapfile   none    swap    sw    0   0" >> /etc/fstab

apt-get update

# Install git
apt-get -y install git
git clone https://github.com/tantalim/scala-server.git

# Install nginx
apt-get -y install nginx
cp conf/nginx.conf /etc/nginx/sites-available/tantalim.com
ln -s /etc/nginx/sites-available/tantalim.com /etc/nginx/sites-enabled/tantalim.com
/etc/init.d/nginx restart

# Install Docker
apt-get -y install docker.io
