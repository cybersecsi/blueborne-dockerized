apt update
apt install -y curl bluez libbluetooth-dev python3 python3-pip
curl https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip --output /home/ngrok.zip
unzip /home/ngrok.zip
pip3 install pybluez pwn

