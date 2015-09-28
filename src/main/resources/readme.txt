crontab -e
5 13 * * * /var/auto/mail-task.sh > /dev/null 2>&1

#!/bin/bash
cd /var/auto
java -cp mail-task.jar SimpleBlog.App
#touch $(date -u +%s)
