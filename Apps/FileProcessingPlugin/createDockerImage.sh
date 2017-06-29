../gradlew clean build installDist
if [ $# -eq 0 ]
  then
    docker build -t matrixapps/fileprocessingplugin .
else
    docker build -t matrixapps/fileprocessingplugin:"$1" .
fi
