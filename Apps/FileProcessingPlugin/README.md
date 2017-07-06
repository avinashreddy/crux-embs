# eMBS Server

### Overview ###
The eMBS Server polls the eMBS FTP server for new signal files (.SIG) under /Signal directory. For every .SIG file found a message of type is sent to the pipeline. 
The [FileProcessingPlugin](../FileProcessingPlugin) picks up the message and loads the file to Crux. Ad-hoc requests can also be sent from the Reflex REPEL.  

# Installation and Running #
Following containers must be running. Refer [Matrix Setup](../../README.md). 
1. incapture/rabbit
2. incapture/postgres
3. incapture/mongo
4. matrixapps/matrixapiserver
5. matrix-ui
6. incapture/elasticsearch 


**Install FileProcessingPlugin**
```
docker run -e HOST=curtis  -e PASSWORD=rapture -e OVERLAY=dev --link curtis --link postgres --name fileprocessingplugin avinashreddy/fileprocessingplugin 
```
The plugin contains the eMBS workflow. It also installs configuration required for eMBS Server. Update cruxApiurl, cruxApiKey and cruxDatasetId in document://configs/crux/embs/workflow/config. eMBS FTP configuration can be updated in document://configs/crux/ftp/config 

**Start eMBS Server** 
```
docker run -eUI_URL=http://localhost:8000 -e SERVER_ENV=DEV -d --link mongo --link rabbit --link elasticsearch --link postgres -p 62933:62933 -e SERVER_PROFILE=DEV -e JAVA_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,address=62933,server=y,suspend=n' --name embsserver avinashreddy/embsserver
```
Check the status of eMBS Server by running **docker logs -f embsserver**.  You should see the following message at the bottom:
> "*** EMBSServer successfully started ***"

This completes installation. 

There is a Job that runs every minute to check for signal files. This is currently not working. Run the script script://scripts/crux/ftp/embs/pollftp instead to process files.
A work order is created for each .ZIP file processed.  