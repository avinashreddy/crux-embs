../gradlew clean build installDist
cp -r ~/Curtis/docker/curtis/rootfs rootfs/
if [ $# -eq 0 ]
  then
    docker build -t matrixapps/embserver .
else
    docker build -t matrixapps/embserver:"$1" .
fi
