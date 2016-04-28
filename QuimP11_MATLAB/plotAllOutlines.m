function plotAllOutlines( cells )
%PLOTALLOUTLINES Summary of this function goes here
%   Detailed explanation goes here

    
    %   PLOTOUTLINE(C, COLOUR, MARKERSIZE, LINESIZE)
    colour = 'k';
    linewidth = 1;
    saveEPS = true;
    imageHeight = 30;
    imageWidth  = 30;


    nbCells = length(cells);
    
    figure(67);
    hold off;
        
    for c = 1:nbCells,
    
        nFrames = cells(c).nbFrames;

        for i = 1:nFrames,
        
            plotOutline( cells(c).outlines{i}, colour, linewidth  );
            hold on;
  
        end
    
    end
    
    set(gca,'YDir','reverse');
    axis off
    axis equal

    
    if(saveEPS),
        set(gcf, 'color', 'none');
        set(gcf, 'PaperUnits', 'centimeters');
        set(gcf, 'PaperSize', [imageWidth imageHeight]);
        set(gcf, 'PaperPositionMode', 'manual');
        set(gcf, 'PaperPosition', [0 0 imageWidth imageHeight]);
        set(gcf, 'InvertHardcopy', 'off');

        saveas(gcf, ['./' cells(1).name '_outlines.eps'],'epsc');
    end
        
end

