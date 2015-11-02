function plotMotility(c,maxMigration,varargin)
% QuimP11 function
% PLOTMOTILITY plot a heat map of cell motility
%
%   PLOTMOTILITY(C , MAXMIGRATION) plots the cell outline C with nodes
%   coloured according to their speed of movement. MAXMIGRATION is a scalar 
%   defining the maximum speed of migration and is used to scale the 
%   colour map.
%   
%   PLOTMOTILITY(C , MAXMIGRATION, MARKERSIZE)
%   
%   PLOTMOTILITY(C , MAXMIGRATION, MARKERSIZE, LINESIZE)

    mSize = 4;
    lSize = 1;
    drawContour = 0;

    op = size(varargin,2);

    if(op == 0),
    elseif(op == 1),
        mSize = varargin{1,1};
    elseif (op==2),
        mSize = varargin{1,1};
        lSize  = varargin{1,2};
        drawContour = 1;
    end
    
    % cheack its not the whole snake
    if( size(c,2) < 6),
        fprintf('Error: The first argumnet needs to be a single outline:(outlines{i})\n');
        return;
    end

    colRes = 512;
    colREmap = colormap(jet(colRes));
    
    if(maxMigration > 0), 
        colConvert = (colRes/2) / maxMigration;
    else
        colConvert = (colRes/2);
    end
    
    
    h = ishold();
    
    if(drawContour),
        plot(c(:,2), c(:,3), '-k', 'LineWidth',lSize); 
        hold on;
    end
    
    for i = 1:size(c,1),
        
        l = floor(abs(c(i,6)) * colConvert);
        if(l==0), l = l +1;end
 
        if(l >256),
            warning('MATLAB:outOfRange','max migration not the max!');
            l = 256;
        end    
        if(c(i,6) > 0),
            col =  colREmap(l+256,:);
        else
            col =  colREmap((257-l),:);
        end
        
        plot(c(i,2), c(i,3), 'o', 'Color',col,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize); 
        hold on;
    end
    axis equal;
    
    if(h==0), hold off; end
    
end