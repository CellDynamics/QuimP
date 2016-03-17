function [stats, fluoStats, sHeaders, fHeaders] = readStats(fileName),
% QuimP11 function
% READSTATS  Reads data from '.stQP.csv' file
    %
    %   [STATS, FLUOSTATS, STATSHEADERS, FLUOHEADERS] = READSTATS(FILEPATH)
    %   returns the statistics in the file located at FILEPATH.
    %   
    %   STATS is a num.FRAMES by 11 matrix containing whole cell statistics
    %   for each frame.  For example, STATS(5,4) is the displacement at
    %   frame 5. STATSHEADERS is cell containing the description for each
    %   column of data (e.g. STATSHEADERS{4} = 'Displacement').
    %
    %   FLUOSTATS is a num.FRAMES by 3 by 11 matrix, containing the whole
    %   cell fluorescence statistics for each frame. The dimensions are as
    %   follows:
    %   [FRAME, CHANNEL, DATA],
    %   where DATA is the required statistic as described in FLUOHEADERS.

    sHeaders = {'1.Frame','2.x-Centroid','3.Y-Centroid','4.Displacement','5.Distance Traveled','6.Directionality','7.Speed','8.Perimeter','9.Elongation','10.Circularity','11.Area'};
    fHeaders = {'1.Frame', '2.Total Fluo.','3.Mean Fluo.','4.Cortex Width', '5.Cyto. Area','6.Total Cyto. Fluo.', '7.Mean Cyto. Fluo.','8.Cortex Area','9.Total Cortex Fluo.', '10.Mean Cortex Fluo.', '11.%age Cortex Fluo.'};
    
    
    
    INFILE = fopen(fileName); %open the file
    if(INFILE==-1), fprintf('Warning: Failed to read stats file %s.\n',fileName); stats = [];fluoStats=[];return;end;

%     % check version
%     oldFormat = false;
%     line = fgetl(INFILE);
%     if( ~strcmp(line, '#p2') ),
%         % not new version
%         fprintf('\tOld format stats file\n');
%         oldFormat = true;
%     end
    
    exampleGot = false;
    frames = 0; % count the number of data lines
    while (feof(INFILE)~=1), 
        line = fgetl(INFILE);
        if (line(1) ~= '#'),    % skip comment lines
            frames = frames +1;
            if(~exampleGot), eg = line; exampleGot = true; end
        end
    end
    
    realFrames = frames / 4;
   
    INFILE=fclose(INFILE);
   
    % count number of columns
    nbCol = 1;
    for c = 1:length(eg),
        if(eg(c)==','), nbCol=nbCol+1; end
    end
    
    % determin version. old == so columns, new == 11
    if(nbCol == 20), 
        oldFormat = true; 
    else
        oldFormat = false;
    end
    
    INFILE = fopen(fileName);
    
    stats = zeros(frames, nbCol);
    rr = ['%d' repmat(',%f',1,nbCol-1)];
    
    f = 1;
    while (feof(INFILE)~=1),
        line = fgetl(INFILE);
        if (line(1) ~= '#'), 
            read = sscanf(line,rr, nbCol);
            stats(f,:) = read';
            f=f+1;
        end
    end

    if(oldFormat),
        fluoStats = zeros(frames, 3, 11).*-1;
        fluoStats(:,1,1) = 1:frames; % frames
        fluoStats(:,1,[2:3,5:11]) = stats(:, 12:20); % 4 is unknown cortex width
        stats = stats(:,1:11);
    else
        fluoStats = zeros(realFrames, 3, nbCol).*-1;
        fluoStats(:,1,:) = stats(realFrames+1 : realFrames*2, :);
        fluoStats(:,2,:) = stats(realFrames*2+1 : realFrames*3, :);
        fluoStats(:,3,:) = stats(realFrames*3+1 : realFrames*4, :);
        stats = stats(1:realFrames,:);
    end
    
    fclose(INFILE);
    

end