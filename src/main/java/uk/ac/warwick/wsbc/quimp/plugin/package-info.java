/**
 * This package contains interfaces that define QuimP plugins.
 * <h1>Introduction</h1>
 * <p>
 * The QuimP plugin is a module available from the QuimP Toolbar. In future those modules can be
 * provided as separate jars and be discoverable by QuimP Toolbar.
 * <h2>Plugins</h2>
 * <p>
 * There are two kinds of QuimP plugins:
 * <ol>
 * <li>Utilised by BOA module - those must implement
 * {@link uk.ac.warwick.wsbc.quimp.plugin.snakes.IQuimpBOAPoint2dFilter} or
 * {@link uk.ac.warwick.wsbc.quimp.plugin.snakes.IQuimpBOASnakeFilter}</li>
 * <li>General purpose plugins - they constitute extensible QuimP Toolbar. They implement
 * {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin}</li>
 * </ol>
 * General purpose plugins differ from standard IJ plugins based only on
 * {@link ij.plugin.filter.PlugInFilter} or {@link ij.plugin.PlugIn} interfaces (those are extended
 * by {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin} and
 * {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPluginFilter} respectively. General purpose plugins
 * are meant to be used with QCONF file platform. If plugin works like standard IJ plugin it should
 * implement only IJ interfaces or IQuimp interfaces but it can be based on e.g.
 * {@link uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate}.
 * 
 * General plugins can be run from IJ macro scripts therefore they also implement
 * {@link ij.plugin.PlugIn} interface (via {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin}),
 * and their architecture should follow IJ guidelines:
 * <ol>
 * <li>Default constructor should do nothing for running the plugin
 * <li>Other constructor are allowed, for example for testing
 * <li>The main runner is {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin#run(String)} method.
 * Input parameter for {@link uk.ac.warwick.wsbc.quimp.plugin.IQuimpPlugin#run(String) }can be used
 * for passing parameters from e.g. parameterised constructor. It is not that parameter that contain
 * options given in IJ macro scripts. It can be empty or null.
 * <li>Plugin should be able to run options given in IJ macro - in this case without UI and with
 * showing all messages in console.
 * <li>If plugin uses {@link ij.gui.GenericDialog} the IJ macro recorder will discover it and its
 * parameters. If plugin uses other UI framework, support for macros lays on developer (see
 * {@link uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate}).
 * </ol>
 * <p>
 * <h1>Plugin template</h1> Abstract class {@link uk.ac.warwick.wsbc.quimp.plugin.PluginTemplate}
 * contains basic code that supports macros and calls from UI. <b>Only for plugins that interfere
 * with QCONF platform</b>
 * 
 * @author p.baniukiewicz
 */
package uk.ac.warwick.wsbc.quimp.plugin;