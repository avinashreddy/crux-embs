#!/bin/bash
stunnel /opt/stunnel/stunnel.conf &
/opt/rapture/FixServer/bin/FixServer
