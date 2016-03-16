level = 910;  //centre of distribution
width = 950;  // curent ditribution width
stretch = 600;  // amount to stretch

max = 4095;

//scale = level / (level - crop);
scale = (width+stretch)/width;
sep = 0;

getDimensions(width, height, channels, slices, frames);


for(px = 0; px <width; px++){
	for(py = 0; py <height; py++){
		value = getPixel(px, py);
		if(value > level){
			sep =  value - level;		
			value =  level + round(sep * scale);
			if(value < 0) value = 0;
			setPixel(px, py, value);
		}else if(value < level){
			sep =  level-value;		
			value =  level - round(sep * scale);
			if(value > max) value = max;
			setPixel(px, py, value);
		}
			
	}
}


//makeRectangle(354, 0, 318, 512);

//makeRectangle(0, 0, 354, 512);
