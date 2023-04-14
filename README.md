Pit Market 2.0
==============

This repo is mainly maintained by siegmund42. Contact him for bugfixes/contributions.

A summary of information about the game is available [here](https://tu-dresden.de/gsw/wirtschaft/cepe/research/Pit-Market-2.0). If you want to learn more about the economic background of the game, take a look at the corresponding [paper](https://ideas.repec.org/p/zbw/tudcep/0416.html)

##How to build
1. Clone repository
2. Install _maven3_
3. Run `maven3 package` in the cloned repo
4. Executable .jar can be found in the _target_ folder


##How to integrate the project into Eclipse

1. Start Eclipse
2. File -> Import
3. Git -> Project from Git -> Clone URI
  * URI: https://github.com/freeDom-/jKMS
  * Host: github.com (should be filled in automatically)
  * Repository path: freeDom-/jKMS (should be filled in automatically)
  * Protocol: https
  * Port: (leave it empty)
  * Fill in your Username and Password for GitHub
4. Select all branches
5. Select a directory your project should be cloned to
6. Import existing project
7. Finish


##Used frameworks, technologies

*	Springboot 1.1.8
*	jQuery 2.1.1, jQuery UI 1.12.0
*	flot library 0.8.3
*	itextpdf 5.5.13.3
*	opencsv 2.3
*	maven3
