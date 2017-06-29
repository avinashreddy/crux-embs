#!/bin/bash
if [ -z "${OVERLAY}" ] ; then
	/opt/rapture/FileProcessingPlugin/bin/FileProcessingPlugin -host http://$HOST:8665/rapture -password $PASSWORD "$@"
else
	/opt/rapture/FileProcessingPlugin/bin/FileProcessingPlugin -host http://$HOST:8665/rapture -password $PASSWORD -overlay $OVERLAY "$@"
fi
