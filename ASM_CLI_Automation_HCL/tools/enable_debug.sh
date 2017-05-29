#!/bin/sh

set -o errexit

# Script to run on the appliance to make it friendlier for remote debugging:
#   - opens external postgresql access on port 5432
#   - start tomee in debug mode, and opens access to it on port 8000
#   - "fixes" yum to allow yum inlstalls to work

echo "Configuring postgresql to allow external access..."

# Configure postgresql to allow network-based access to all hosts
# instead of just to localhost
sed -i.orig -e s,127.0.0.1/32,all, /opt/Dell/asmdb/data/pg_hba.conf

# Reload the changed configuration
echo "Reloading postgresql configuraiton..."
service asmdb reload

# Open the firewall to allow access to postgresql
lokkit -p 5432:tcp

# Open the firewall to allow access to razor and ASM REST interfaces
lokkit -p 8080:tcp
lokkit -p 8081:tcp
lokkit -p 9080:tcp

# Configure tomee start scripts to start in debug mode
ASM_INIT_FILE=/etc/init.d/tomeeASM
if ! grep startup.sh $ASM_INIT_FILE > /dev/null; then
    echo "TomEE debug configuration already in place; skipping..."
else
    echo "Changing tomeeASM to start with 'catalina.sh jpda start'..."

    # Use "catalina.sh jpda start" instead of startup.sh to start in
    # debug mode
    sed -i.bak -e 's,startup.sh,catalina.sh jpda start,' $ASM_INIT_FILE
    
    # Disable REST authentication
    DISABLE_AUTH_OPT=com.dell.asm.restapi.core.filters.authenticationDisabled
    SETENV_FILE=/opt/Dell/ASM/bin/setenv.sh
    if ! grep "$DISABLE_AUTH_OPT" "$SETENV_FILE" > /dev/null; then
	echo "Disabling REST authentication..."
	cp -a "$SETENV_FILE" "$SETENV_FILE.bak"
	echo "CATALINA_OPTS=\"\$CATALINA_OPTS -D$DISABLE_AUTH_OPT=true\"" >> "$SETENV_FILE"
    fi

    # Restart TomEE with change
    echo "Restarting TomEE..."
    service tomeeASM restart

    # Open the firewall to allow debug access on port 8000
    lokkit -p 8000:tcp
fi

# Fix yum
MIRROR=http://mirror.rackspace.com/CentOS/6
ORIG_MIRROR=http://linux.dell.com/repo/software/ASM/latest/centos/6
REPO_FILE=/etc/yum.repos.d/ASM.repo

if ! grep "$ORIG_MIRROR" "$REPO_FILE" > /dev/null; then
    echo "Yum repo debug configuration already in place; skipping..."
else
    echo "Configuring yum to access public CentOS repo..."
    # Replace the linux.dell.com mirror with a working one
    sed -i.bak -e "s,$ORIG_MIRROR,$MIRROR," "$REPO_FILE"
    
    # Create a script that makes it easier to install packages
    cat <<EOF > ~/bin/yum.sh
#!/bin/bash

set -o errexit

http_proxy=http://proxy.us.dell.com:80/
https_proxy=http://proxy.us.dell.com:80/
ftp_proxy=http://proxy.us.dell.com:80/

export http_proxy https_proxy ftp_proxy

yum --disablerepo=* --enablerepo=base --enablerepo=updates \$*

EOF
    chmod ugo+x ~/bin/yum.sh
    echo "Use ~/yum.sh to install centos packages"
fi
