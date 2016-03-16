function [startCoor, BW] = findPeaks(grad, peakThresh, meanThreh)
    doPlot = 1;
    
    %peakThresh = 0.33; %seqHfps paper 0.33
    %meanThreh = 0.24; % seqHfps paper 0.24
    
    %grad(grad < 0.4) = 0;

    [oY,oX]=size(grad);
    grad = [grad,grad,grad];

    %gaus = fspecial('gaussian',3,3)
    %grad = imfilter(grad,gaus); % smooth
    
 
    BW = imextendedmax(grad,peakThresh); % find peak regions
    
    if(doPlot),
        figure(14)
        subplot(1,2,1);
        imagesc(grad(:, oX+1 : oX + oX + 1));
        %plotMap(grad(:, oX+1 : oX + oX + 1));
        title('Displacement map');
        subplot(1,2,2);
        imagesc(BW(:, oX+1 : oX + oX + 1));
        %tempBW = BW(:, oX+1 : oX + oX + 1);
        title('Peaks in displacement');
    end
    
    peaks = zeros(size(grad));

    [BW, NUM] = bwlabeln(BW');   % label peak regions (transpose to label from top down);
    BW = BW';
    
    [Y,X] = size(grad);
    yc = repmat((1:Y)',1,X); %x and y coordinate matrices
    xc = repmat((1:X),Y,1);
    
    startCoor = zeros(NUM,2);
 
    for i = 1:NUM, % find centre of regions
       
       singleR = grad; 
       singleR(BW ~= i) =0; %select region i
       
       % calc centre of gravity
       total = sum(sum(singleR)); % mean of region not above threshold
       if(total/sum(sum(BW==i)) < meanThreh),
           startCoor(i,:) = [-1,-1];
       else
  
         
         %y = sum(sum((yc .* singleR) ./ total)); % frames
         %y=round(y);
         
         %singleR( 1:round(y)-1,:) = 0; % just use centre 3 frames to get x weighted centre
         %singleR( round(y)+1:oY,:) = 0;
         %total = sum(sum(singleR));
         %x = sum(sum((xc .* singleR) ./ total));
         
         %[~,x] = max( singleR(y,:));
         
         [mV,y] = max(singleR);
         [~,x] = max( mV);
         y = y(x);
         
         peaks(round(y),round(x)) = 1;
         startCoor(i,:) = [round(y),round(x)];
       end
    end
    
    startCoor(startCoor(:,1)==-1, :) =[]; % remove ones bwlow thresh
    
    peaks = peaks(:,oX+1:oX*2);
    startCoor(startCoor(:,2)>oX*2, :) = [];
    startCoor(startCoor(:,2)<=oX, :) = [];
    startCoor(:,2) = startCoor(:,2) - oX;
    
    if(doPlot),
        hold on
        subplot(1,2,2);
        plot(startCoor(:,2), startCoor(:,1), 'og', 'MarkerSize', 8);
        % colour first half green to see label order
        plot(startCoor(1:floor(end/2),2), startCoor(1:floor(end/2),1), 'ow', 'MarkerSize', 8);
        hold off
        
        subplot(1,2,1);
        hold on
        plot(startCoor(:,2), startCoor(:,1), 'xw', 'MarkerSize', 8);
        % colour first half green to see label order
       % plot(startCoor(1:floor(end/2),2), startCoor(1:floor(end/2),1), 'ow', 'MarkerSize', 8);
        
        hold off
        %close;
        
        % for paper
        figure(87)
        imagesc(grad(:, oX+1 : oX + oX + 1));
        hold on
        plot(startCoor(1:(end-1),2), startCoor(1:(end-1),1), 'ow', 'MarkerSize', 7.5, 'LineWidth', 1.5);
        axis off
       % reverseAxis;
        hold off
    end
    
    BW = BW';
end





