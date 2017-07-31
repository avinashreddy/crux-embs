#!/bin/bash
if [ -z "${OVERLAY}" ] ; then
	/opt/rapture/FileProcessingConfigPlugin/bin/FileProcessingConfigPlugin -host http://$HOST:8665/rapture -password $PASSWORD "$@"
else
	/opt/rapture/FileProcessingConfigPlugin/bin/FileProcessingConfigPlugin -host http://$HOST:8665/rapture -password $PASSWORD -overlay $OVERLAY "$@"
fi
