/**
 * <h1>About</h1>
 * 
 * This plugin converts Snakes stored in QCONF file to binary masks.
 * 
 * <h2>Prerequisites</h2>
 * 
 * Snakes produced by BOA plugin.
 * 
 * <h2>Compatibility</h2>
 * 
 * Only QCONF.
 * 
 * <h2>Macro support</h2>
 * 
 * Macro supported.
 * 
 * <h3>Parameters</h3>
 * 
 * <ol>
 * <li>none - will open file selector
 * <li><i>paramFile</i>=path_to_qconf - file to process.
 * </ol>
 * <p>
 * 
 * <pre>
 * {@code run("Generate mask","{paramFile:[RW/C1-talA_mNeon_bleb_0pt7%agar_FLU_fine.QCONF]}") }
 * </pre>
 * 
 * Output file <b>will be</b> saved on disk as well.
 * 
 * <h2>API support</h2>
 * 
 * Supported. See test case.
 * 
 * @author p.baniukiewicz
 *
 */
package com.github.celldynamics.quimp.plugin.generatemask;