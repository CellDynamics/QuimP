function plotMap( map, type, FI, varargin)
% Quimp 11 function
% PLOTMAP    Plot a .maQP map
%
%      PLOTMAP(M, TY, FI) plots the map specified by M,
%      where TY is:
%            'm' - motility map
%            'f' - fluorescence map
%            'c' - curvature map
%      and FI is the frame interval (seconds).  The colour mapping is
%      is scaled according to the largest value in M (max(max(abs(M)))).
%
%      PLOTMAP(M, TY, FI, CU) scales the colour map to an upper limit of
%      CU, and a lower limit of -CU.
%
%      PLOTMAP(M, TY, FI, CU, CL) scales the colour map to an upper limit
%      of CU, and a lower limit of -CL.

    op = size(varargin,2);
    
    if( op == 0),
        % defaults
        [Llim, Ulim] = calcLimits(map);
    elseif(op == 1),
        Llim = -varargin{1,1};
        Ulim = varargin{1,1};
    elseif(op == 2),
        Llim = varargin{1,1};
        Ulim = varargin{1,2};
    else
        error('Too many arguments');
    end
    
    
    %plot and save otions--------
        savePlot = false;
        cMapRes = 512; % colour resolution
    
        if(strcmp(type, 'm')),
            t = 'Motility Map';
            cMap = jet(cMapRes); % colors
            colbarLabel = ['Speed (', char(181), 'm/s)' ];
            fileName = [datestr(now, 'yymmdd_HH:MM:SS') '_MotPlot.eps'];
        elseif(strcmp(type, 'd')),
            t = 'Displacement Map';
            cMap = jet(cMapRes); % colors
            colbarLabel = ['Displacement (', char(181), 'm)' ];
            fileName = [datestr(now, 'yymmdd_HH:MM:SS') '_MotPlot.eps'];
        elseif(strcmp(type, 'f')),
            t = 'Fluorescence Map';
            colbarLabel = 'Intensity';
            fileName = [datestr(now, 'yymmdd_HH:MM:SS') '_FluoPlot.eps'];
            cMap = hot(cMapRes); % colors
            Llim = 0;
        else,
            t = 'Curvature Map';
            colbarLabel = 'Curvature';
            fileName = [datestr(now, 'yymmdd_HH:MM:SS') '_CurvPlot.eps'];
            cMap = jet(cMapRes); % colors
        end

        path = '/temp/';
        paperSizeX = 5;
        paperSizeY = 7.5;
        
    %----------------
    
    
    ytickspaceframes = round(size(map,1)/10); 
    
    cLim = getNewClim(1,cMapRes, Llim, Ulim,length(cMap));
       
    
    
    IM = image( map );
    sM = gca;
    %scrsz = get(0,'ScreenSize');
    %set(gcf,'Position',[1, scrsz(4)/1.5, 400, 300]);
    
    xlim=get(sM, 'XLim');
    xticks = xlim(1):100:xlim(2);
    set(gca, 'XTick', xticks);
    set(gca, 'XTickLabel', ( 0:1/(length(xticks)-1):1 ) ); 
    
    ylim=get(sM, 'YLim');
    yticks = (ylim(1):ytickspaceframes:ylim(2) )
    set(gca, 'YTick', yticks);
    
    yLabels = ((yticks+0.5).*FI) - FI
    set(gca, 'YTickLabel', yLabels); 
    
    xlabel('Cell Outline');
    ylabel('Time (seconds)');
    colormap(cMap);
    set(IM,'CDataMapping', 'scaled');
    caxis(cLim);
    cb=colorbar();
    % axis(cb,[0,1,Llim,Ulim]);  % disabled as causing problems with new
    % matlab

    
    %color bar width
    ax1=get(gca,'position');
    cbx=get(cb,'Position');
    
    cbx(3)=0.03;
    set(cb,'Position',cbx);
    set(gca,'position',ax1);
    
    title(t);
    set( get(gca,'Title'), 'FontSize', 12);
    %cbLab = ylabel(cb,['Speed (', char(181), 'm/s)' ]);
    cbLab = ylabel(cb, colbarLabel);
    set(gcf, 'color', 'white');
    set(gca,'FontSize',8)
    set( get(gca,'XLabel'), 'FontSize', 10);
    set( get(gca,'YLabel'), 'FontSize', 10);
    set(cbLab, 'FontSize',10);
    
    if(savePlot),
        saveas(gcf,[path fileName '.fig'],'fig');
        savePlot([path fileName], paperSizeY, paperSizeX);
    end
end

function [L , U] = calcLimits(map),
    
    U = max(max(abs(map)));
    L = -U;

   % L = min(min(map));
    
    %U = max(max(map));
   % L = -max(abs(L),abs(U));
    %U = -L;
end


function CLim = getNewClim(BeginSlot,EndSlot,CDmin,CDmax,CmLength)
   % 				Convert slot number and range
   % 				to percent of colormap
   PBeginSlot    = (BeginSlot - 1) / (CmLength - 1);
   PEndSlot      = (EndSlot - 1) / (CmLength - 1);
   PCmRange      = PEndSlot - PBeginSlot;
   % 				Determine range and min and max 
   % 				of new CLim values
   DataRange     = CDmax - CDmin;
   ClimRange     = DataRange / PCmRange;
   NewCmin       = CDmin - (PBeginSlot * ClimRange);
   NewCmax       = CDmax + (1 - PEndSlot) * ClimRange;
   CLim          = [NewCmin,NewCmax];
end



