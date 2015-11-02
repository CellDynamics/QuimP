# This script starts Fiji application with debugger
# Fiji and JRE have to exist beyond source tree
JAVA_DIR="jdk1.8.0_51"
RED='\033[1;31m'
NC='\033[0m' # No Color
echo -e ${RED}*********************************${NC}
echo -e ${RED}* Started with java $JAVA_DIR *${NC}
echo -e ${RED}*********************************${NC}
echo
Fiji.app/ImageJ-linux64 --debugger=8000 --java-home /home/baniuk/$JAVA_DIR
