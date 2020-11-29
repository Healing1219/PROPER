sudo mkdir ./lp_solve_5.5
sudo tar -zxvf lp_solve_5.5.0.14_dev_ux64.tar.gz -C ./lp_solve_5.5
sudo cp ./lp_solve_5.5_java/lib/ux64/liblpsolve55j.so /usr/local/lib
sudo cp ./lp_solve_5.5/liblpsolve55.so /usr/local/lib
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
