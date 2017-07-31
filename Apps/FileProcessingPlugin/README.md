# eMBS Server

### Overview ###
The eMBS Server polls the eMBS FTP server for new signal files (.SIG) under /Signal directory. For every .SIG file found a message of type is sent to the pipeline. 
The [FileProcessingPlugin](../FileProcessingPlugin) picks up the message and loads the file to Crux. Ad-hoc requests can also be sent from the Reflex REPEL. 
 The [FileProcessingConfigPlugin](../FileProcessingConfigPlugin) contains configuration files and must be deployed first - before eMBS Server and FileProcessingPlugin.  

# Installation and Running #
Following containers must be running. Refer [Matrix Setup](../../README.md). 
1. incapture/rabbit
2. incapture/postgres
3. incapture/mongo
4. matrixapps/matrixapiserver
5. matrix-ui
6. incapture/elasticsearch 

**Install FileProcessingConfigPlugin**
```
docker run -e HOST=curtis  -e PASSWORD=rapture -e OVERLAY=dev --link curtis --link postgres --name fileprocessingconfigplugin avinashreddy/fileprocessingconfigplugin
```
The plugin installs configuration required for eMBS Server and FileProcessingPlugin. Update cruxApiurl, cruxApiKey and cruxDatasetId in document://configs/crux/embs/workflow/config. eMBS FTP configuration can be updated in document://configs/crux/ftp/config 


**Install FileProcessingPlugin**
```
docker run -e HOST=curtis  -e PASSWORD=rapture -e OVERLAY=dev --link curtis --link postgres --name fileprocessingplugin avinashreddy/fileprocessingplugin 
```
The plugin contains the eMBS workflows. 

**Start eMBS Server** 
```
docker run -eUI_URL=http://localhost:8000 -e SERVER_ENV=DEV -d --link mongo --link rabbit --link elasticsearch --link postgres -p 62933:62933 -e SERVER_PROFILE=DEV -e JAVA_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,address=62933,server=y,suspend=n' --name embsserver avinashreddy/embsserver
```
Check the status of eMBS Server by running **docker logs -f embsserver**.  You should see the following message at the bottom:
> "*** EMBSServer successfully started ***"

This completes installation. 

There is a Job that runs every minute to check for signal files. This is currently not working. Run the script script://scripts/crux/ftp/embs/pollftp instead to process files.
This is what happens when the script is executed

* The script drops a message on the pipeline. The message contains the list of file to process. 
* The message is picked up by eMBS server.
* eMBS Server groups files that map to the same Crux table. An eMBS file maps to only one table but multiple files can map to the same table.
* eMBS send two types of messages to the pipleline depending on how files are grouped. A FileProcessingRequest for groups that have only one file. A FileGroupProcessingRequest
  for groups with more than one file. BigQuery does not support updates to tables. But eMBS data is updates regularly. Grouping of files is required only when updated are involved.
* FileProcessingRequest is picked up by workflow://workflows/crux/embs/processFile. And FileGroupProcessingRequest is picked up by workflow://workflows/crux/embs/processFileGroup workflow.

**Use Case 1:**    
We have two .SIG file on eMBS FTP. The files map to different tables and the tables are empty. When we run the script. 
1. The script drops a message on the pipeline. 
2. EMBS server picks it up and for every file listed in the message it looks for a .SIG file. In this case there is two .SIG file. 
3. Since the files belong to separate groups two groups a are created. 
4. EMBS Server drops two FileProcessingRequests on the pipeline - one for each file.
5. processFile workflow picks up the messages and inserts recors into corresponding tables. Since the tables are empty an update is not required.       

**Use Case 2:**    
We have two .SIG file on eMBS FTP. The files map to different tables and the tables are not empty. When we run the script. 
* Steps 1 to 4 same as use case 1.
5. processFile workflow picks up the messages and updates the table. Since BigQuery does not support updates an update is achieved as follows
      * Create a temp tables
      * insert all records on file into temp table
      * Insert all records on target table that are not in temp tables into temp table - insert into temp where id not in (select id from target).
      * Overwrite target with temp.  

**Use Case 3:**    
 We have two .SIG file on eMBS FTP. The files map to the same table and the table is empty. When we run the script. 
 1. The script drops a message on the pipeline. 
 2. EMBS server picks it up and for every file listed in the message it looks for a .SIG file. 
 3. Since the files belong to the same group . 
 4. EMBS Server drops a single FileGroupProcessingRequest on the pipeline - for the two files.
 5. processFileGroup workflow picks up the message.
 6. processFileGroup checks if the target table is empty. Since the target table is empty two FileProcessingRequests are dropped on the pipeline. 
    The workflow completed.
 7. The two FileProcessingRequests are processed just like use case 1 but the target table is the same.    
 
 **Use Case 4:**    
  We have two .SIG file on eMBS FTP. The files map to the same table and the table is not empty. When we run the script. 
  * Steps 1 to 5 same as use case 3.
  6. processFileGroup workflow checks if the target table is empty. Since the target table is not it first drops two FileProcessingRequests. The FileProcessingRequests
     do not contain a temp table as the target table and not the actual target table. 
  7. The two FileProcessingRequests are processed just like use case 3 but the target table is a temp table.
  8. processFileGroup workflow waits for the two FileProcessingRequests to complete.     
  9. The target table is updated. 
         * Insert all records on target table that are not in temp tables into temp table - insert into temp where id not in (select id from target).
         * Overwrite target with temp. 

         
