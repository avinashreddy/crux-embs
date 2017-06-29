{
  "workflowURI" : "workflow://workflows/crux/embs/processFile",
    "semaphoreType" : "WORKFLOW_BASED",
    "semaphoreConfig" : "{\"maxAllowed\":100}",
  "steps" : [
  {
    "name" : "configure",
    "description" : "Read configuration parameters from a document",
    "executable" : "dp_java_invocable://embs.ConfigurationStep",
    "transitions" : [ {
      "name" : "next",
      "targetStep" : "downloadFile"
    }, {
      "name" : "",
      "targetStep" : "$FAIL"
    } ],
    "softTimeout" : -1,
    "jarUriDependencies" : [ ]
  },
  {
    "name" : "downloadFile",
    "description" : "download file from FTP",
    "executable" : "dp_java_invocable://embs.DownloadFileStep",
    "transitions" : [ {
      "name" : "next",
      "targetStep" : "transformFile"
    }, {
      "name" : "",
      "targetStep" : "$FAIL"
    } ]
  },
    {
      "name" : "transformFile",
      "description" : "Transform zip file to gzip. Add timestamp and source columns.",
      "executable" : "dp_java_invocable://embs.TransformFileStep",
      "transitions" : [ {
        "name" : "next",
        "targetStep" : "uploadFile"
      }, {
        "name" : "",
        "targetStep" : "$FAIL"
      } ]
    },
    {
      "name" : "uploadFile",
      "description" : "upload file to crux",
      "executable" : "dp_java_invocable://embs.UploadFileStep",
      "transitions" : [ {
        "name" : "next",
        "targetStep" : "loadFileToTable"
      }, {
        "name" : "",
        "targetStep" : "$FAIL"
      } ]
    },
    {
      "name" : "loadFileToTable",
      "description" : "upload file to crux",
      "executable" : "dp_java_invocable://embs.LoadFileToTableStep",
      "transitions" : [ {
        "name" : "next",
        "targetStep" : "successNotificationStep"
      }, {
        "name" : "",
        "targetStep" : "$FAIL"
      } ]
    },
  {
      "name" : "successNotificationStep",
      "description" : "description",
      "executable" : "dp_java_invocable://embs.AlertStep",
      "view" : {
        "MESSAGE_SUBJECT" : "$DATA_ARCHIVE_SUCCESS_SUBJECT"
      },
      "transitions" : [ {
        "name" : "next",
        "targetStep" : "$RETURN"
      }, {
          "name" : "",
          "targetStep" : "$FAIL"
      }]
    }
  ],
  "startStep" : "configure",
  "jarUriDependencies" : ["jar://workflows/dynamic/*"],
  "category" : "embs",
    "view": {
        "JOBNAME": "warehouseJob",
        "CONFIGURATION" : "[\"document://configs/crux/embs/workflow/config\", \"document://configs/crux/embs/workflow/messages\"]"
    }
}
