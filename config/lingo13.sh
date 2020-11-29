sudo tar -zxvf ./LINGO-LINUX-64x86-13.0.tar.gz
chmod 755 $HOME/PROPER/config/lingo13/bin/linux64/
export LD_LIBRARY_PATH=$HOME/PROPER/config/lingo13/bin/linux64:$LD_LIBRARY_PATH
export LINGO_13_HOME=$HOME/PROPER/config/lingo13
cd $HOME/PROPER/config/lingo13/bin/linux64
sudo ./lingovars.sh
sudo ./symlinks.sh
cd $HOME/PROPER/config/lingo13
sudo sh create_demo_license.sh
export PATH=$PATH:$HOME/PROPER/config/lingo13/bin/linux64
cd $HOME/PROPER/config

