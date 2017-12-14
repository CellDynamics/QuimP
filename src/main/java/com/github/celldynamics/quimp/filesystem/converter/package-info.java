/**
 * Contain tools for conversion between old and new files.
 * 
 * <h1>About</h1>
 * 
 * This plugin performs conversion between old and new data format and allows for extreacting data
 * from new format.
 * 
 * <h2>Prerequisites</h2>
 * 
 * Old or new configuration file.
 * 
 * <h2>Macro support</h2>
 * 
 * Macro is supported.
 * 
 * <h3>Parameters</h3>
 * 
 * If status list is empty, plugin performs conversion of loaded file to opposed format. If it is
 * not empty, specified fields from new file are extracted. See
 * {@link com.github.celldynamics.quimp.filesystem.converter.FormatConverterModel}
 * 
 * <h2>API support</h2>
 * 
 * Supported, see tests.
 * 
 * @author p.baniukiewicz
 *
 */
package com.github.celldynamics.quimp.filesystem.converter;