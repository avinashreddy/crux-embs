../gradlew clean build installDist
if [ $# -eq 0 ]
  then
    docker build -t matrixapps/fileprocessingconfigplugin .
else
    docker build -t matrixapps/fileprocessingconfigplugin:"$1" .
fi
