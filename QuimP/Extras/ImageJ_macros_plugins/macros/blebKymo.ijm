macro 'blebKymo'{

	i = getSliceNumber();
	numFrames = i+18;

	for( ; i <= numFrames ;){
		setSlice(i);
		
		i++;
	}


}