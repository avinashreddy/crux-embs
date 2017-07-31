{
  "workflowURI" : "workflow://workflows/crux/embs/processFileGroup",
    "semaphoreType" : "WORKFLOW_BASED",
    "semaphoreConfig" : "{\"maxAllowed\":100}",
  "steps" : [
  {
    "name" : "configure",
    "description" : "Read configuration parameters from a document",
    "executable" : "dp_java_invocable://embs.ConfigurationStep",
    "transitions" : [ {
      "name" : "next",
      "targetStep" : "publishFileProcessingRequestsStep"
    }, {
      "name" : "",
      "targetStep" : "$FAIL"
    } ],
    "softTimeout" : -1,
    "jarUriDependencies" : [ ]
  },
    {
      "name" : "publishFileProcessingRequestsStep",
      "description" : "Create Crux table if it does not exist",
      "executable" : "dp_java_invocable://embs.filegroup.PublishFileProcessingRequestsStep",
      "transitions" : [ {
        "name" : "next",
        "targetStep" : "waitForRequestProcessingToCompleteStep"
      }, {
       "name" : "finish",
       "targetStep" : "$RETURN"
      } , {
        "name" : "",
        "targetStep" : "$FAIL"
      } ]
    },
  {
    "name" : "waitForRequestProcessingToCompleteStep",
    "description" : "download file from FTP",
    "executable" : "dp_java_invocable://embs.filegroup.WaitForRequestProcessingToCompleteStep",
    "transitions" : [ {
      "name" : "next",
      "targetStep" : "updateTableStepStep"
    }, {
      "name" : "",
      "targetStep" : "$FAIL"
    } ]
  },
    {
      "name" : "updateTableStepStep",
      "description" : "upload file to crux",
      "executable" : "dp_java_invocable://embs.filegroup.UpdateTableStepStep",
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
        "JOBNAME": "EMBSDataloadJob",
        "CONFIGURATION" : "[\"document://configs/crux/embs/workflow/config\", \"document://configs/crux/embs/workflow/messages\"]"
    }
}
