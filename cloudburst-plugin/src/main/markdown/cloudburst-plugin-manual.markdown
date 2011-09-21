% Deployit WebSphere CloudBurst Plugin Manual
%
% April, 2011

# Preface

This manual describes the Deployit WebSphere CloudBurst Plugin.

# Prerequisite reading

* Read the [Deployit System Administration Manual](http://docs.xebialabs.com/releases/3.0.1/systemadminmanual.html) to learn how to install and set up Deployit.
* Read the [Deployit Graphical User Interface (GUI) Manual](http://docs.xebialabs.com/releases/3.0.1/guimanual.html) to learn how to use Deployit.

# Software versions

* WebSphere CloudBurst Appliance 2.x (tested with 2.0.0.2)
* WebSphere Application Server 7.x (tested with 7.0.0.11)
* Deployit 3.0.x (tested with 3.0.2)

# Limitations

* Can only deploy to WebSphere Application Server single server with standalone profile
* Can deploy application artifacts and resources to first image in a virtual system only.
* Can only deploy and undeploy a single EAR artifact. Upgrade is not supported.

# Set up your environment

1. Install the [CloudBurst command line interface (CLI)](http://publib.boulder.ibm.com/infocenter/wscloudb/v2r0/topic/com.ibm.websphere.cloudburst.doc/cc/cct_usingcli.html) on a machine, e.g. the machine that Deployit runs on. The Deployit server should be able to access the CloudBurst CLI *and* the CloudBurst CLI should be able to access to the WebSphere CloudBurst Appliance.
2. Create a Host configuration item (CI) for the host on which you installed the CloudBurst CLI. For example, if you installed the CloudBurst CLI on the machine on which Deployit runs:
	* ID: `Infrastructure/wcacli`
	* Operating System Family: `UNIX` (if the local machine is a UNIX machine)
	* Access Method: `LOCAL`
	* Address: `localhost`
3. Under the Host CI, create a `CloudBurstAppliance` CI for the WebSphere CloudBurst Appliance. For example:
	* ID: `Infrastructure/wcacli/wca`
	* CLI Home: the path to the CloudBurst CLI installation, e.g. `/opt/IBM/cloudburst.cli`
	* Address: the address of the WebSphere CloudBurst appliance, e.g. `wca`
	* Username: the administrative username for the CloudBurst appliance, e.g. `cbadmin`
	* Password: the administrative password
4. Create an Environment CI that contains the WebSphere CloudBurst Appliance that you have just created:
	* ID: `Environments/CloudBurst`
	* Members: `wca`

# Import the application package

1. Import the following package from the server:
* `PetClinic-ear/1.0`

# Deploy the application package to a newly created virtual system on the WebSphere CloudBurst appliance

1. Open the Initial Deployment screen in the Deployit UI.
2. Drag the `PetClinic-ear/1.0` package to the left side of the screen.
3. Drag the CloudBurst environment to the right side of the screen.
4. In the middle, drag the `PetClinic-1.0.ear` on the left to the `wca` item on the right.
5. Double-click the `PetClinic-1.0.ear` item that appears underneath the `wca` item.
6. Enter the following values in the popup balloon that appears:
	* Pattern name: The name of the pattern to deploy, e.g. `WebSphere single server`
	* Cloud group name: The name of the cloud group to deploy the pattern to, e.g. `Application on-demand group`
	* System name: The name of the virtual system to be created, e.g. `PetClinic system`
	* System password: The administrative password that will be assigned to your operating system and your middleware
7. Click the "Save" button at the bottom of the popup.
8. Click the "Next" button at the bottom the deployment screen.
9. Click the "Deploy" button at the bottom of the next screen.
10. Deployit will now instruct the WebSphere CloudBurst Appliance to deploy the pattern, read the information about the virtual system. discover the WAS installation on it and then deploy and start the EAR file.

# Destroy the virtual system

1. Select the deployed application `PetClinic-ear/1.0` underneath the CloudBurst environment in the Deployed Applications browser on the right hand side.
2. Right-click and select "Undeploy".
3. Click the "Undeploy" button that appears on the next screen.
4. Deployit will now instruct WCA to destroy the virtual system.

# CloudBurst Configuration Items (CIs)

The CloudBurst Plugin defines configuration items (CIs) needed to deploy to the WebSphere CloudBurst appliance. To get more information about these CIs, use Deployit's command line interface (CLI). See the **Deployit Command Line Interface (CLI) Manual** for more information.
