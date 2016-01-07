run("Invert");
run("Enhance Contrast", "saturated=0.35");
run("Median Light");
run("Inhomogen Isotropic Diffusion 2D", "lambda=10 iterations=30 dt=0.10");
run("Find Edges");
setAutoThreshold("Default dark");
//run("Threshold...");
setThreshold(16, 19);
run("Convert to Mask");
run("Dilate");
run("Dilate");
run("Dilate");
run("Fill Holes");
run("Erode");
run("Erode");

