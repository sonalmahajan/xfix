# XFix: A Tool for Automated Repair of Layout Cross Browser Issues (XBIs)

Cross Browser Issues (XBIs) are inconsistencies in the appearance or behavior of a website across different browsers. Although XBIs can impact the appearance or functionality of a website, the vast majority --- over 90% --- result in appearance related problems. This makes XBIs a significant challenge in ensuring the correct and consistent appearance of a website’s UI. A *layout XBI* is any XBI that relates to an inconsistent layout of HTML elements in a web page when viewed in different browsers. Despite the importance of XBIs, there is little tool support for their repair. To address these limitations, we propose *XFix* that uses guided search-based techniques to automatically repair layout XBIs in websites. More algorithmic details of XFix can be found in our paper:
```
Automated Repair of Layout Cross Browser Issues Using Search-Based Techniques
Sonal Mahajan, Abdulmajeed Alameer, Phil McMinn, William G. J. Halfond
In Proceedings of the 26th International Symposium on Software Testing and Analysis (ISSTA). July 2017. Acceptance rate: 26%.
https://dl.acm.org/citation.cfm?doid=3092703.3092726
```
## How to run XFix?
1. **Inputs:** Run  [TestXFix.java](https://github.com/sonalmahajan/xfix/blob/master/src/test/java/eval/TestXFix.java) from the xfix project by passing the following inputs as Strings:<br />
	(a) URL of the page under test (PUT)<br />
	(b) File system location of the PUT<br />
	(c) Reference browser (FIREFOX/CHROME/INTERNET_EXPLORER) that shows the correct layout of the PUT<br />
	(d) Test browser (FIREFOX/CHROME/INTERNET_EXPLORER) that shows a layout inconsistency with respect to the reference browser.

2. **Output:** The output produced by XFix can be found in the parent folder of the location provided in input (b):<br />
	(a) log_<timestamp>.txt: stores the detailed log information of one run of the subject. The generated repair patch and summary of the timing results and XBI reduction can be found at the end of this file.<br />
	(b) repair.css: Generated CSS file with the repair patch to fix the observed XBIs. The repair patches are generated specific to the test browser with the relative fix values applied to the CSS properties and their absolute values shown in comments.<br />
	(c) test_fixed.html: Modified PUT with the repair.css file applied.
	
## Evaluation Data
#### Our patched version of X-PERT: 
Our patched version of the X-PERT tool can be found [here](http://atlanta.usc.edu:8081/artifactory/ext-release-local/patched-xpert/patched-xpert/1.0.0/) as a jar file. The jar contains a "README.txt" with details of the defects that were corrected and the accessor methods that were added. The jar can be unpackaged using the command `jar xf patched-xpert-1.0.0.jar`.

#### Subjects: 
The 15 real-world web pages used in the evaluation of XFix can be downloaded [here](https://drive.google.com/file/d/0B5pAs3GeZs4sRXhQYWtPUmc2LXc/view?usp=sharing).

#### Human Study Data: 
The *reference*, *before*, and *after* versions of the 15 subjects shown to the human study participants can be found [here](https://drive.google.com/open?id=0B5pAs3GeZs4sbFdfWG4wdXdabk0). Open index.html from the subjects folder to navigate through the screenshots. The participant marked (circled) areas of visual differences can be found [here](https://drive.google.com/open?id=0B5pAs3GeZs4sZUZBX2lSRHZLdU0).

## Questions
In case of any questions you can email at spmahaja [at] usc {dot} edu
