# Requirements and installation

QuimP is written in Java as a set of ImageJ plugins, thus it can be run on any platform that supports Java (Windows, MacOS, Linux). A fast processor and at least 4 GB of RAM is recommended (more in case of large datasets). 

# Basic requirements

The following components are required to run QuimP:
- ImageJ (or Fiji) in version 1.43 or later.  
- QuimP core package - it can be downloaded from [here](http://www2.warwick.ac.uk/fac/sci/systemsbiology/staff/baniukiewicz/quimp/quimp-download)
- _**Java 8** or newer runtime (system-wide installation)_ - only if you are upgrading old installation, see details below.

QuimP will also function with Fiji, an extended version of ImageJ, available here: http://pacific.mpi-cbg.de/wiki/index.php/Fiji.

~~Note that ImageJ as well as Fiji are shipped with embedded Java in version 6. To make QuimP working on these (default) installations, one must reconfigure them according to [this FAQ](http://imagej.net/Frequently_Asked_Questions) in order to promote **Java 8** over default **Java 6**.~~

Since May 2016 Fiji officially supports Java 8. Every new installation comes with bundled **Java 8**, old installations can be also upgraded, see [this link] (http://imagej.net/2016-05-10_-_ImageJ_HOWTO_-_Java_8,_Java_6,_Java_3D) for details.   

## Installation procedure for fresh installations

1. Download and install ImageJ or [Fiji](http://fiji.sc/#download) (recommended).
1. Download QuimP in latest available version from [here](http://www2.warwick.ac.uk/fac/sci/systemsbiology/staff/baniukiewicz/quimp/quimp-download/)
1. Unpack the content of `QuimP-xxxx.zip` archive (`xxxx` string stands for current QuimP version) into `plugins` folder located in the installation directory of ImageJ.
1. **Remove any previous Quimp_ package from `plugins`**
1. Run ImageJ/Fiji and head to the `Plugnis` menu. The `QuimP-xxxx` entry should be available there.

## Installation procedure for existing installations

1. Download and install **Java 8**. For MacOS or Linux extra configuration steps may be necessary. Consult documentation relevant to your system.
1. Consult [Fiji FAQ](http://imagej.net/Frequently_Asked_Questions) how to run ImageJ or Fiji with **Java 8**.
1. Download QuimP in latest available version from [here](http://www2.warwick.ac.uk/fac/sci/systemsbiology/staff/baniukiewicz/quimp/quimp-download/)
1. Unpack the content of `QuimP-xxxx.zip` archive (`xxxx` string stands for current QuimP version) into `plugins` folder located in the installation directory of ImageJ.
1. **Remove any previous Quimp_ package from `plugins`**
1. Run ImageJ/Fiji and head to the `Plugnis` menu. The `QuimP-xxxx` entry should be available there.
