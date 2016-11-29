/*
 * //!>
 * 
 * @startuml doc-files/stats_2_UML.png
 * actor user
 * user -> user : //create DataContainer dt//
 * user -> StatsCollection : //<create>//
 * activate StatsCollection 
 * StatsCollection --> user : pointer
 * user -> StatsCollection : ""copyFromCellStat(CellStatsEval)""
 * user -> Serializer : //<create>//, ""dt""
 * activate Serializer
 * user -> Serializer : ""save()""
 * @enduml
 * 
 * //!<
 */

/*
 * //!>
 * @startuml doc-files/stats_1_UML.png
 * CellStatsEval *-- "1" CellStats
 * CellStats o-- "1..*" FrameStatistics
 * StatsCollection o-- "1..*" CellStats
 * 
 * note top of CellStatsEval : Exchange class\nWrites files and delivers\nstat ""CellStats"" object.
 * note top of StatsCollection : Keep ""CellStats"" for every cell\nin DataContainer.
 * note left of CellStats : Keep stats for every cell\n in ""FrameStatistics""
 * note left of FrameStatistics : Keep numeric data for every\n frame for one cell.
 * @enduml
 * 
 * //!<
 */
/**
 * This is the main package.
 * 
 * Below there are fragments of QuimP architecture.
 * 
 * <h1>Architecture</h1>
 * <h2>Generating statistic files</h2>
 * 
 * Statistics generated by QuimP are related to cell shape and fluorescence distribution along cell
 * cortex. They are stored in <i>stQP.csv</i> and <i>QCONF</i> files but they are build on different
 * stages of processing. The general class relationship is as follows:<br>
 * <img src="doc-files/stats_1_UML.png"/><br>
 * {@link uk.ac.warwick.wsbc.QuimP.CellStatsEval} depending how it is created writes statistics to
 * disk as stQP files or only computes them internally (they are computed as well for first case).
 * They can be obtained by calling {@link uk.ac.warwick.wsbc.QuimP.CellStatsEval#getStatH()} which
 * provides {@link uk.ac.warwick.wsbc.QuimP.CellStats} object that holds statistics for one cell for
 * all frames (as List of {@link uk.ac.warwick.wsbc.QuimP.FrameStatistics} objects. DataContainer
 * holds List of {@link uk.ac.warwick.wsbc.QuimP.CellStats} for every cell in experiment. Fitting
 * statistic object into {@link uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer}:<br>
 * <img src="doc-files/stats_2_UML.png"/><br>
 * Relevant part of code:<br>
 * 
 * <pre>
 * <code>
 *     DataContainer dt = new DataContainer(); // create container
 *     dt.BOAState = qState; // assign boa state to correct field
 *     dt.Stats = new StatsCollection();
 *     dt.Stats.copyFromCellStat(ret); // StatsHandler is initialized here.
 *     n = new Serializer<>(dt, quimpInfo);
 *     if (qState.boap.savePretty) // set pretty format if configured
 *         n.setPretty();
 *     n.save(qState.boap.deductNewParamFileName());
 * </code>
 * </pre>
 *
 * <h1>Code rules</h1>
 */
package uk.ac.warwick.wsbc.QuimP;