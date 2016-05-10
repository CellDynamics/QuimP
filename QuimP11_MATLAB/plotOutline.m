function plotOutline(c1,varargin)
% QuimP11 function
% PLOTCONTOUR  Plots cell outlines
%
%   PLOTOUTLINE(C) plots the cell outline C.  C can be in the form of an
%   N by 6 matrix, where N is the number of nodes, C(:,2) are the x-coordinates
%   and c(:,3) are the y-coordinates (as outputted by READQANALYSIS 
%   and READOUTLINES). C may also be an N by 2 matrix, where C(:,1) are the
%   x-coordinates and C(:,2) the y-coordinates.
%
%   PLOTOUTLINE(C, COLOUR)
%
%   PLOTOUTLINE(C, COLOUR, LINESIZE)
%
%   PLOTOUTLINE(C, COLOUR, LINESIZE, MARKERSIZE)


col = 'k';
mSize = 2;
lSize = 0.3;
useMarkers = 0;
linesmooth = 'on';

if verLessThan('matlab','8.3') 
    linesmooth_str =  {'LineSmoothing',linesmooth};
else
    linesmooth_str = {}; % do not use LineSmoothing property as it is unofficial.
end

op = size(varargin,2);

if(op == 0),
elseif(op == 1),
    col = varargin{1,1};
elseif (op==2),
    col = varargin{1,1};
    lSize  = varargin{1,2};
elseif (op >=3),
    col = varargin{1,1};
    mSize  = varargin{1,3};
    lSize  = varargin{1,2};
    useMarkers = 1;
end

h = ishold();
% plot contour

if(useMarkers),
    if(size(c1,2) > 2),
        % plot data from snake 
        plot([c1(1,2), c1(end,2)],[c1(1,3), c1(end,3)],'-o','color',col,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
        hold on;
        plot(c1(:,2), c1(:,3),'-o', 'color', col ,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
    else
        %just plot as 2 column matrix
        plot([c1(1,1), c1(end,1)],[c1(1,2), c1(end,2)],'-','color',col,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
        hold on;
        plot(c1(:,1), c1(:,2),'-', 'color', col ,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
    end
else
    if(size(c1,2) > 2),
        % plot data from snake 
        plot([c1(1,2), c1(end,2)],[c1(1,3), c1(end,3)],'-','color',col,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
        hold on;
        plot(c1(:,2), c1(:,3),'-', 'color', col ,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
    else
        %just plot as 2 column matrix
        plot([c1(1,1), c1(end,1)],[c1(1,2), c1(end,2)],'-','color',col,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
        hold on;
        plot(c1(:,1), c1(:,2),'-', 'color', col ,'MarkerFaceColor',col,'MarkerSize',mSize,'LineWidth',lSize, linesmooth_str{:});
    end
end
    
axis equal

if(h==0), hold off; end


end