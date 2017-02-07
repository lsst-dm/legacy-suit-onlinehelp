# suit-onlinehelp

Prerequisites
-------------
    - gradle v2.2+
    - clone onlinehelp repository and its dependent repository
        - git clone https://github.com/lsst/suit-onlinehelp
        - git clone https://github.com/Caltech-IPAC/firefly


Build and Install Individually
------------------------------
- cd suit-onlinehelp
- gradle :<project_name>:build      // build only
    - creates an archive of html and supporting files to be install to a webserver
    - the file is placed in ./build/libs/

- gradle :<project_name>:install    // build and install.
    - crates and install online help files
    - HTML_DOC_ROOT environment variable is required to locate the path to the webserver's document root.

- gradle projects                   // for a list of project names

gradle will also take partial names as long as it can uniquely resolve it.
So, instead of typing 'gradle :suit:build' you can type
    - gradle :su:bui