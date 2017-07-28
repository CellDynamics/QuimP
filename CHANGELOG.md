# Change Log

## [v17.07.01](https://github.com/CellDynamics/QuimP/tree/v17.07.01) (2017-07-28)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.06.02...v17.07.01)

**Implemented enhancements:**

- Fix polar plots [\#264](https://github.com/CellDynamics/QuimP/issues/264)
- Optimise stat saving [\#262](https://github.com/CellDynamics/QuimP/issues/262)
- Add invert option to DIC [\#261](https://github.com/CellDynamics/QuimP/issues/261)
- Add copy from previous option [\#259](https://github.com/CellDynamics/QuimP/issues/259)
- Snakes created from binary masks do not have properties set  [\#256](https://github.com/CellDynamics/QuimP/issues/256)

**Fixed bugs:**

- Add conversion for STATS [\#175](https://github.com/CellDynamics/QuimP/issues/175)

## [v17.06.02](https://github.com/CellDynamics/QuimP/tree/v17.06.02) (2017-06-16)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.06.01...v17.06.02)

**Implemented enhancements:**

- Update documentation [\#257](https://github.com/CellDynamics/QuimP/issues/257)
- Combine RW and AC [\#206](https://github.com/CellDynamics/QuimP/issues/206)

**Fixed bugs:**

- Save & Quit fails if only one frame is segmented [\#255](https://github.com/CellDynamics/QuimP/issues/255)
- Store BOA filter config in QCONF [\#254](https://github.com/CellDynamics/QuimP/issues/254)
- Hamcrest is obsolete [\#253](https://github.com/CellDynamics/QuimP/issues/253)

## [v17.06.01](https://github.com/CellDynamics/QuimP/tree/v17.06.01) (2017-06-05)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.05.01...v17.06.01)

**Implemented enhancements:**

- Add Iterator to Shape [\#252](https://github.com/CellDynamics/QuimP/issues/252)
- Better filtration in HatSnakeFilter  [\#245](https://github.com/CellDynamics/QuimP/issues/245)

## [v17.05.01](https://github.com/CellDynamics/QuimP/tree/v17.05.01) (2017-05-24)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.04.04...v17.05.01)

**Fixed bugs:**

- SVG circles are not plotted [\#251](https://github.com/CellDynamics/QuimP/issues/251)
- Field 'frozen' is not included in copy constructor [\#250](https://github.com/CellDynamics/QuimP/issues/250)
- HatSnakeFilter throws exception if there is no input image [\#249](https://github.com/CellDynamics/QuimP/issues/249)
- CorrectDensity can finish before processing all nodes [\#248](https://github.com/CellDynamics/QuimP/issues/248)
- TrackOutline - first and last points are the same [\#247](https://github.com/CellDynamics/QuimP/issues/247)
- Fix running mean filter in OutlineProcessor [\#246](https://github.com/CellDynamics/QuimP/issues/246)

## [v17.04.04](https://github.com/CellDynamics/QuimP/tree/v17.04.04) (2017-04-28)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.04.03...v17.04.04)

**Implemented enhancements:**

- Disable FG/BG competition [\#241](https://github.com/CellDynamics/QuimP/issues/241)
- Merge results into one table [\#240](https://github.com/CellDynamics/QuimP/issues/240)

**Fixed bugs:**

- Show seeds shows wrong frame [\#243](https://github.com/CellDynamics/QuimP/issues/243)
- Binary mask import behaves incorrectly [\#242](https://github.com/CellDynamics/QuimP/issues/242)

## [v17.04.03](https://github.com/CellDynamics/QuimP/tree/v17.04.03) (2017-04-11)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.04.02...v17.04.03)

**Implemented enhancements:**

- Add stopping criterion based on RelError [\#238](https://github.com/CellDynamics/QuimP/issues/238)
- Hat filter should filter all protrusions above acceptance level [\#236](https://github.com/CellDynamics/QuimP/issues/236)

**Fixed bugs:**

- Lack of file separator [\#237](https://github.com/CellDynamics/QuimP/issues/237)

## [v17.04.02](https://github.com/CellDynamics/QuimP/tree/v17.04.02) (2017-04-03)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.04.01...v17.04.02)

**Fixed bugs:**

- On paQP-\>QCONF conversion last frame of BOA parameters has default values [\#235](https://github.com/CellDynamics/QuimP/issues/235)

## [v17.04.01](https://github.com/CellDynamics/QuimP/tree/v17.04.01) (2017-03-31)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.03.02...v17.04.01)

**Implemented enhancements:**

- Rework getQuimPBuildInfo [\#84](https://github.com/CellDynamics/QuimP/issues/84)
- Button Load in BOA should work with QCONF [\#232](https://github.com/CellDynamics/QuimP/issues/232)
- Copy plugin to all frames [\#184](https://github.com/CellDynamics/QuimP/issues/184)

**Fixed bugs:**

- Closing RW window causes IJ to end [\#234](https://github.com/CellDynamics/QuimP/issues/234)
- Update scale interval and image scale on QCONF load [\#233](https://github.com/CellDynamics/QuimP/issues/233)
- FormatConverter loses scale in paQP [\#146](https://github.com/CellDynamics/QuimP/issues/146)

## [v17.03.02](https://github.com/CellDynamics/QuimP/tree/v17.03.02) (2017-03-22)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/BioinformaticsV...v17.03.02)

**Implemented enhancements:**

- Add error criterion to Random Walker segmentation [\#113](https://github.com/CellDynamics/QuimP/issues/113)
- Add option for segmenting current frame only [\#230](https://github.com/CellDynamics/QuimP/issues/230)
- Improve detection of vesicles [\#229](https://github.com/CellDynamics/QuimP/issues/229)
- Add saving in old format as configurable option [\#228](https://github.com/CellDynamics/QuimP/issues/228)

**Fixed bugs:**

- Accidental bug when sliding over slices [\#197](https://github.com/CellDynamics/QuimP/issues/197)

## [BioinformaticsV](https://github.com/CellDynamics/QuimP/tree/BioinformaticsV) (2017-03-17)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.03.01...BioinformaticsV)

## [v17.03.01](https://github.com/CellDynamics/QuimP/tree/v17.03.01) (2017-03-14)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/Bioinformatics...v17.03.01)

**Implemented enhancements:**

- Implement local mean in RW [\#226](https://github.com/CellDynamics/QuimP/issues/226)
- Add suport for GSon versioning [\#220](https://github.com/CellDynamics/QuimP/issues/220)

**Fixed bugs:**

- Image scale does not update [\#227](https://github.com/CellDynamics/QuimP/issues/227)
- Message in log window after Prot Analysis [\#189](https://github.com/CellDynamics/QuimP/issues/189)

## [Bioinformatics](https://github.com/CellDynamics/QuimP/tree/Bioinformatics) (2017-03-13)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.02.04...Bioinformatics)

## [v17.02.04](https://github.com/CellDynamics/QuimP/tree/v17.02.04) (2017-02-16)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.02.03...v17.02.04)

**Implemented enhancements:**

- ANA results should be displayed in IJ table [\#225](https://github.com/CellDynamics/QuimP/issues/225)

**Fixed bugs:**

- When trying to use "Add cell" errors are produced [\#222](https://github.com/CellDynamics/QuimP/issues/222)
- Frame unzoom does not work on windows [\#193](https://github.com/CellDynamics/QuimP/issues/193)
- QCONF keeps absolute paths [\#118](https://github.com/CellDynamics/QuimP/issues/118)

## [v17.02.03](https://github.com/CellDynamics/QuimP/tree/v17.02.03) (2017-02-07)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.02.02...v17.02.03)

**Fixed bugs:**

- Contour shrinking behaviour in ANA  [\#224](https://github.com/CellDynamics/QuimP/issues/224)

## [v17.02.02](https://github.com/CellDynamics/QuimP/tree/v17.02.02) (2017-02-06)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/v17.02.01...v17.02.02)

**Fixed bugs:**

- Test QuimP in the QuimP menu produces error [\#221](https://github.com/CellDynamics/QuimP/issues/221)
- orgFile:path is not updated on QCONF save [\#219](https://github.com/CellDynamics/QuimP/issues/219)

**Merged pull requests:**

- Massive changes of JavaDocs [\#218](https://github.com/CellDynamics/QuimP/pull/218) ([baniuk](https://github.com/baniuk))

## [v17.02.01](https://github.com/CellDynamics/QuimP/tree/v17.02.01) (2017-01-30)
[Full Changelog](https://github.com/CellDynamics/QuimP/compare/GitHub...v17.02.01)

**Implemented enhancements:**

- Add option for real-time updating segmentation [\#213](https://github.com/CellDynamics/QuimP/issues/213)
- Add scijava pom as parent pom [\#212](https://github.com/CellDynamics/QuimP/issues/212)
- Use threads for RW [\#208](https://github.com/CellDynamics/QuimP/issues/208)
- Add smart shrinking [\#205](https://github.com/CellDynamics/QuimP/issues/205)
- Change webpage in quimp info [\#204](https://github.com/CellDynamics/QuimP/issues/204)
- Add convert masks plugin to plugin menu [\#198](https://github.com/CellDynamics/QuimP/issues/198)
- Suppress standard console messages [\#188](https://github.com/CellDynamics/QuimP/issues/188)
- Plugin for rendering masks from QCONF [\#183](https://github.com/CellDynamics/QuimP/issues/183)
- Clone copies the whole stack - user should be notified that he must provide stack of seed [\#179](https://github.com/CellDynamics/QuimP/issues/179)
- Modules produce paQP files if new format selected [\#173](https://github.com/CellDynamics/QuimP/issues/173)
- ANA throws unnecessary exception if no file opened [\#172](https://github.com/CellDynamics/QuimP/issues/172)
- Add perpendicular averaging for DIC [\#170](https://github.com/CellDynamics/QuimP/issues/170)
- Replace log4j [\#168](https://github.com/CellDynamics/QuimP/issues/168)
- Add warning before use of Formatconverter [\#166](https://github.com/CellDynamics/QuimP/issues/166)
- Add showing registration window option to Bar [\#165](https://github.com/CellDynamics/QuimP/issues/165)
- Licence info [\#160](https://github.com/CellDynamics/QuimP/issues/160)
- Add registration window [\#159](https://github.com/CellDynamics/QuimP/issues/159)
- Add result preview [\#156](https://github.com/CellDynamics/QuimP/issues/156)
- Add more advanced UI to Protrusion tracking [\#152](https://github.com/CellDynamics/QuimP/issues/152)
- Add conversion for ANA and Stats to FormatConverter [\#151](https://github.com/CellDynamics/QuimP/issues/151)
- Add statistics for Protrusion Analysis [\#150](https://github.com/CellDynamics/QuimP/issues/150)
- Add polar plots [\#148](https://github.com/CellDynamics/QuimP/issues/148)
- Plot color outlines according to motility or other parameters around cell [\#147](https://github.com/CellDynamics/QuimP/issues/147)
- Add separate tool to convert paQP to QCONF [\#144](https://github.com/CellDynamics/QuimP/issues/144)
- Static maxima and tracks can be displayed on mean image [\#142](https://github.com/CellDynamics/QuimP/issues/142)
- Add plotting outlines in colors depending on given criteria [\#141](https://github.com/CellDynamics/QuimP/issues/141)
- Add preselection of active file format [\#137](https://github.com/CellDynamics/QuimP/issues/137)
- Rename toolbar menu entry [\#136](https://github.com/CellDynamics/QuimP/issues/136)
- Add description of parameters in paQP [\#134](https://github.com/CellDynamics/QuimP/issues/134)
- Option for saving outlines as separte image [\#127](https://github.com/CellDynamics/QuimP/issues/127)
- Adapt ANA to new file format [\#126](https://github.com/CellDynamics/QuimP/issues/126)
- User friendly message when input image in wrong format [\#125](https://github.com/CellDynamics/QuimP/issues/125)
- QCONF should be generated always together with old format [\#124](https://github.com/CellDynamics/QuimP/issues/124)
- Detect if loaded QCONF to ECMM contains any previous data [\#123](https://github.com/CellDynamics/QuimP/issues/123)
- Add support of QCONF in Q Analysis [\#122](https://github.com/CellDynamics/QuimP/issues/122)
- Add options to clear all snakes and other settings [\#117](https://github.com/CellDynamics/QuimP/issues/117)
- Random Walk GUI should offer option of creating of seed image [\#111](https://github.com/CellDynamics/QuimP/issues/111)
- Add link to doc from QuimP bar [\#110](https://github.com/CellDynamics/QuimP/issues/110)
- Add default log4j configuration file [\#107](https://github.com/CellDynamics/QuimP/issues/107)
- Add about menu in QuimP bar [\#103](https://github.com/CellDynamics/QuimP/issues/103)
- Fake segmentation should use various sources of masks [\#102](https://github.com/CellDynamics/QuimP/issues/102)
- Add creating Snakes directly from masks [\#98](https://github.com/CellDynamics/QuimP/issues/98)
- Implement RandomWalk method as separate filter for QuimP [\#97](https://github.com/CellDynamics/QuimP/issues/97)
- Add color marker for filter list to indicate whether they have been instanced [\#92](https://github.com/CellDynamics/QuimP/issues/92)
- Detect if user loaded correct file for Plugin config and Global config [\#91](https://github.com/CellDynamics/QuimP/issues/91)
- Original Snakes are not restored [\#90](https://github.com/CellDynamics/QuimP/issues/90)
- Add date to QCONF [\#89](https://github.com/CellDynamics/QuimP/issues/89)
- Allow to load new format in BOA [\#87](https://github.com/CellDynamics/QuimP/issues/87)
- Switch to new output format in ECMM [\#83](https://github.com/CellDynamics/QuimP/issues/83)
- Check SnakePluginList for possible bug [\#82](https://github.com/CellDynamics/QuimP/issues/82)
- Add fixed-width fonts in About dialog [\#78](https://github.com/CellDynamics/QuimP/issues/78)
- Set show head to off by default [\#76](https://github.com/CellDynamics/QuimP/issues/76)
- Separate BOAp and SegParam classes [\#69](https://github.com/CellDynamics/QuimP/issues/69)
- Save and restore current BOA state [\#64](https://github.com/CellDynamics/QuimP/issues/64)
- Selecting head node in ECMM [\#61](https://github.com/CellDynamics/QuimP/issues/61)
- Rework QuimP bar [\#60](https://github.com/CellDynamics/QuimP/issues/60)
- Rename Finish to Quit & Save [\#59](https://github.com/CellDynamics/QuimP/issues/59)
- Discard/Apply filter effects for whole stack without segmenting again [\#58](https://github.com/CellDynamics/QuimP/issues/58)
- Add Cancel button in QWindow template [\#55](https://github.com/CellDynamics/QuimP/issues/55)
- About window displays short description of filters [\#54](https://github.com/CellDynamics/QuimP/issues/54)
- Let About window supports RMB [\#53](https://github.com/CellDynamics/QuimP/issues/53)
- Fix support for plugins interfaces [\#48](https://github.com/CellDynamics/QuimP/issues/48)
- Add option for displaying node in QuimP view [\#47](https://github.com/CellDynamics/QuimP/issues/47)
- Option for temporary disabling plugins [\#43](https://github.com/CellDynamics/QuimP/issues/43)
- Add history of actions [\#42](https://github.com/CellDynamics/QuimP/issues/42)
- Add plugin configration saving [\#41](https://github.com/CellDynamics/QuimP/issues/41)
- Add DIC to icon [\#40](https://github.com/CellDynamics/QuimP/issues/40)
- Create results as new images [\#38](https://github.com/CellDynamics/QuimP/issues/38)
- DIC plugin does not report progress [\#37](https://github.com/CellDynamics/QuimP/issues/37)
- Option to show plots of segmented and processed snake [\#35](https://github.com/CellDynamics/QuimP/issues/35)
- Add About button [\#33](https://github.com/CellDynamics/QuimP/issues/33)
- Implement bidirectional communication between BOA and plugin [\#31](https://github.com/CellDynamics/QuimP/issues/31)
- Load plugin jars from disk on BOA run [\#28](https://github.com/CellDynamics/QuimP/issues/28)
- Implement BOA plugin interface [\#26](https://github.com/CellDynamics/QuimP/issues/26)
- Implement simple window builder [\#25](https://github.com/CellDynamics/QuimP/issues/25)
- Integrate DIC filtering into QuimP [\#23](https://github.com/CellDynamics/QuimP/issues/23)
- Review comments for DIC [\#21](https://github.com/CellDynamics/QuimP/issues/21)
- Rework DIC to store precomputed values [\#19](https://github.com/CellDynamics/QuimP/issues/19)
- Fix reference point over time [\#18](https://github.com/CellDynamics/QuimP/issues/18)
- Generalize rotation [\#14](https://github.com/CellDynamics/QuimP/issues/14)
- Move bounding box to separate class [\#13](https://github.com/CellDynamics/QuimP/issues/13)
- Optimize DIC for normal cpu [\#12](https://github.com/CellDynamics/QuimP/issues/12)
- Add rotation without clipping [\#11](https://github.com/CellDynamics/QuimP/issues/11)
- Implement DIC based on matrix [\#10](https://github.com/CellDynamics/QuimP/issues/10)

**Fixed bugs:**

- Fix TrackVisualisationTest [\#215](https://github.com/CellDynamics/QuimP/issues/215)
- Registration window is not displayed correctly [\#211](https://github.com/CellDynamics/QuimP/issues/211)
- Prevent crashing when less than 3 nodes [\#209](https://github.com/CellDynamics/QuimP/issues/209)
- Possibe bug in correct density [\#207](https://github.com/CellDynamics/QuimP/issues/207)
- Exception thrown when slices seeds and show seeds are used [\#203](https://github.com/CellDynamics/QuimP/issues/203)
- Hide exception [\#202](https://github.com/CellDynamics/QuimP/issues/202)
- Double extensions [\#201](https://github.com/CellDynamics/QuimP/issues/201)
- Zoom selector does not update [\#199](https://github.com/CellDynamics/QuimP/issues/199)
- Zoom does not work in all cases [\#192](https://github.com/CellDynamics/QuimP/issues/192)
- BOA adds snake but not display it [\#191](https://github.com/CellDynamics/QuimP/issues/191)
- Error when processing 2 cells in protrusion analysis module [\#190](https://github.com/CellDynamics/QuimP/issues/190)
- Wrong message after loading file [\#185](https://github.com/CellDynamics/QuimP/issues/185)
- DIC failed on 90 deg [\#182](https://github.com/CellDynamics/QuimP/issues/182)
- DIC does not process stacks [\#178](https://github.com/CellDynamics/QuimP/issues/178)
- QCONF and paQP side-saved on new file format path contains wrong fluoro datafiles [\#174](https://github.com/CellDynamics/QuimP/issues/174)
- ECMM batch processing modifies wrong file. [\#171](https://github.com/CellDynamics/QuimP/issues/171)
- Show license does not work [\#167](https://github.com/CellDynamics/QuimP/issues/167)
- Update registration text [\#164](https://github.com/CellDynamics/QuimP/issues/164)
- BOA fails if old plugins are available [\#163](https://github.com/CellDynamics/QuimP/issues/163)
- Remove all white characters before key generation [\#161](https://github.com/CellDynamics/QuimP/issues/161)
- Random Walk reports wrong colors [\#158](https://github.com/CellDynamics/QuimP/issues/158)
- ECMM and ANA fails on Format conversion [\#157](https://github.com/CellDynamics/QuimP/issues/157)
- Protrusion Analysis does not work on standard Fiji instalation [\#154](https://github.com/CellDynamics/QuimP/issues/154)
- Ana adds some strange paths to paQP [\#132](https://github.com/CellDynamics/QuimP/issues/132)
- Add protection against lack of image. [\#131](https://github.com/CellDynamics/QuimP/issues/131)
- Clear stored measurements does not work [\#130](https://github.com/CellDynamics/QuimP/issues/130)
- paQP file may contain bad segmentation parameters [\#128](https://github.com/CellDynamics/QuimP/issues/128)
- BOA does not ask about file to save on SaveQuit [\#116](https://github.com/CellDynamics/QuimP/issues/116)
- Current frame sometimes does not update when action taken in Binary Segmentation plugin [\#115](https://github.com/CellDynamics/QuimP/issues/115)
- Binary Segmentation causes errors when snake is modified [\#114](https://github.com/CellDynamics/QuimP/issues/114)
- FakeSegmentation fails on single slice images [\#112](https://github.com/CellDynamics/QuimP/issues/112)
- Edited snakes are not processed by filters [\#109](https://github.com/CellDynamics/QuimP/issues/109)
- Plugin names do not appear on list [\#106](https://github.com/CellDynamics/QuimP/issues/106)
- Restoring QCONF on BOA in ImageJ causes error [\#105](https://github.com/CellDynamics/QuimP/issues/105)
- Loading QCONF causes massive exceptions sometimes [\#104](https://github.com/CellDynamics/QuimP/issues/104)
- "Open Image" in QuimP bar does not work [\#101](https://github.com/CellDynamics/QuimP/issues/101)
- Fake segmentation window can be duplicated [\#100](https://github.com/CellDynamics/QuimP/issues/100)
- Outline is not refreshing after loading QCONF [\#95](https://github.com/CellDynamics/QuimP/issues/95)
- When global config is loaded opened plugins are not closed [\#94](https://github.com/CellDynamics/QuimP/issues/94)
- Store does not work when full range segmentation [\#93](https://github.com/CellDynamics/QuimP/issues/93)
- QuimP plugins appear in ImageJ plugin menu [\#77](https://github.com/CellDynamics/QuimP/issues/77)
- Unpredictable zoom [\#75](https://github.com/CellDynamics/QuimP/issues/75)
- log4j config not found [\#73](https://github.com/CellDynamics/QuimP/issues/73)
- Fix formatting in About [\#71](https://github.com/CellDynamics/QuimP/issues/71)
- When frame is edited and there is plugin activated on earlier frame this plugin extends to edited frame [\#68](https://github.com/CellDynamics/QuimP/issues/68)
- BOA fails if quitted after start [\#65](https://github.com/CellDynamics/QuimP/issues/65)
- ECMM fails on run [\#62](https://github.com/CellDynamics/QuimP/issues/62)
- Logs in main window does not scroll and numbering is wrong on second BOA run [\#57](https://github.com/CellDynamics/QuimP/issues/57)
- Make list of filters more intuitive [\#56](https://github.com/CellDynamics/QuimP/issues/56)
- ECMM does not keep Node 0 [\#52](https://github.com/CellDynamics/QuimP/issues/52)
- Plugin window does not disapear when BOA finishes [\#51](https://github.com/CellDynamics/QuimP/issues/51)
- Rename menu Plot processed to Plot original [\#44](https://github.com/CellDynamics/QuimP/issues/44)
- Deleting cells is unpredictable [\#34](https://github.com/CellDynamics/QuimP/issues/34)
- Wrong behaviour window when selecting plugin to none [\#32](https://github.com/CellDynamics/QuimP/issues/32)
- Snake should not be created on plugin error [\#27](https://github.com/CellDynamics/QuimP/issues/27)
- plugin.conf and icons not included in QuimP.jar [\#22](https://github.com/CellDynamics/QuimP/issues/22)
- Wrong reconstruciotn on image edges [\#16](https://github.com/CellDynamics/QuimP/issues/16)
- Possible wrong rotation [\#15](https://github.com/CellDynamics/QuimP/issues/15)
- Ana plugin fails when image is created in IJ [\#8](https://github.com/CellDynamics/QuimP/issues/8)
- BOA crashes when two-channel image opened [\#7](https://github.com/CellDynamics/QuimP/issues/7)
- BOA displays message BOA running on next run [\#5](https://github.com/CellDynamics/QuimP/issues/5)
- Active ROI tool changes to line after cell deletion [\#4](https://github.com/CellDynamics/QuimP/issues/4)
- Wrong composition of BOA window [\#3](https://github.com/CellDynamics/QuimP/issues/3)

**Merged pull requests:**

- Fixed \#214 [\#217](https://github.com/CellDynamics/QuimP/pull/217) ([baniuk](https://github.com/baniuk))
- Fixes \#213 [\#216](https://github.com/CellDynamics/QuimP/pull/216) ([baniuk](https://github.com/baniuk))
- Disable unnecessary error log [\#214](https://github.com/CellDynamics/QuimP/pull/214) ([baniuk](https://github.com/baniuk))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*