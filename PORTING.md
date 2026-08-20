# Porting guidelines
Do you want to help port the mod? That's great! Below you can find some general instructions on how to port the mod to your desired version.

**NOTE: Backports are only accepted if they support the latest minor patch of the major version you are trying to port to.** So if you want to port to say 1.7.X, it must support 1.7.10.

### Directory structure and gradle setup
The directory structure contains the loader folder first, followed by the Minecraft version number for that loader. For example, [neoforge/26.2](./neoforge/26.2/) contains gradle variables and version-specific code for the latest Minecraft 26.2.X edition on Neoforge. The [common](./common) folder (and `<loader>/common`) contains code that is commonly used across all versions. The idea is to rely on common code as far as you can, and put all the version-specific code in the version folder.

Once you set up the directory of your choosing, it must be registered as a project in [settings.gradle](./settings.gradle).

### Step 1. Copy over the closest version
Copy an existing version over to your new version folder.

### Step 2. Build the project
Build the project through `./gradlew :<LOADER>:<VERSION>:build`. It will (most likely) fail to compile. But it will generate the sources you need to debug and change code.

### Step 2b. Restart the language server
The Java Language Server might not find the newly extracted sources. Restart it for the Minecraft imports to resolve correctly.

### Step 3. Debug until feature parity is reached
Debug the code (find the errors) and playtest the mod until feature parity is reached. **NOTE**: if you decide to change common code, you **MUST** build and playtest all versions on the piece of logic you adjusted!

### Step 4. Commit new version on separate branch and create pull request
Commit your new version on a separate branch named `port/<LOADER>-<VERSION>` (for example, `port/forge-1.7`) and open a pull request to have it merged into main. Notify the owner of the repo to double-check it and it will be merged in due time.

