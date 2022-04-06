#!/bin/bash
STARS="**********************"

for i in 1 2 3 4 5
do
        case $i in
        1)
                echo "${STARS}"
                echo "listing dir"
                echo "${STARS}"
                ls -ln
                sleep 3
                ;;

        2)
                echo "${STARS}"
                echo "checking disk usage"
                echo "${STARS}"
                df -h
                sleep 3
                ;;

        3)
                echo "${STARS}"
                echo "checking cpu usage"
                echo "${STARS}"
                top -n1
                sleep 3
                ;;

        4)
                echo "${STARS}"
                echo "listing process"
                echo "${STARS}"
                ps aux
                echo sleep 3
                ;;
        5)
                echo "${STARS}"
                echo "listing connections"
                echo "${STARS}"
                netstat -natp
                sleep 3
                ;;
        *)
                echo -n "unknown"
                ;;
        esac

done

