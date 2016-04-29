function qCells = readQanalysis( varargin )
% QuimP11 function
% READQANALYSIS    Reads QuimP output from a given directory
%
%   [C] = READQANALYSIS() prompts for a directory and will read in all 
%   QuimP analyses found in the choosen directory, and sub directories.
%   C is an array of structures, each element holding data for a single
%   cell (a single Q analysis)
%
%   [C] = READQANALYSIS(P) will read data from the directory path specified 
%   by P
%
% %   [C] = READQANALYSIS(P, S) will read data from the directory path 
%   specified by P, where S is either 'true' or 'false'. If S evaluates 
%   as 'false', sub directories will not be searched.
%
%   The function searches a directory (and it's sub directories), for
%   '.paQP' files.  If found, it attempts to read in as much as the
%   available data as possible with missing data left blank.
%   
%   (is backward compatible with QuimP10 output)

    fprintf('\nRead Q Analysis\n');
    
    op = size(varargin,2);
    searchSubDir = true;

    if(op == 0),
        p = uigetdir(userpath, 'Read Q analysis in directory'); 
        if(p==0), return; end;
    elseif(op == 1),
        p = varargin{1,1};
    elseif(op==2),
        p = varargin{1,1};
        searchSubDir  = varargin{1,2};
    else,
        error('Too many arguments');
    end
    
    if(~strcmp(p(end),filesep)), p = [p filesep]; end % put in file seperator if not there already
    
    % check p is a directory
    if( ~isdir(p) ),
        error('The specified path is not a directory');
    end
    
    fprintf('Searching in "%s"', p);
    if(searchSubDir), fprintf('\n\t...and its sub directories'); end
    fprintf('\n');
   
    files = scoutDirectory(p, searchSubDir);
    nbParams = length(files);
    
    if(nbParams == 0),
        error('Path is incorrect, or does not contain a parameter (.paQP) file\n');
    end
    
    fprintf('Found %d parameter files\n', nbParams);
    
    %qCells = struct([]); % will hold all the analysed cells
    %qCells = zeros(nbParams, 1);
    
    for i = 1:nbParams,
        p = files{i}.p;
        name = files{i}.name;
        fprintf('Reading analysis: %s\n', name); 
        qCells(i) = readpaQP(p, name);
        qCells(i).index = i;
    end
    

end

function files = scoutDirectory(p, searchSubDir )
    % get paths to paQP files
    
    %fprintf('\tlooking in "%s"\n', p);
    dirPa = dir([p '/*.paQP']);
    nbParam = length(dirPa);
    files = cell(nbParam,1);
    
    for i = 1:nbParam,
        f.p = p;
        f.name = dirPa(i).name;
        files(i) = {f};
    end
    
    if(~searchSubDir), return; end
    
    directory = dir(p);
    for i = 1:length(directory),
        if(strcmp(directory(i).name,'.') || strcmp(directory(i).name,'..')),
            continue;
        end
        if( directory(i).isdir),
            files = [files ; scoutDirectory([p directory(i).name filesep], searchSubDir ) ];
        end
    end
end

function qc =  readpaQP(p, paFile)
    qc.name= regexprep( paFile, '.paQP', '' );
    qc.index = [];
    
    % files
    qc.PATH = p;
    qc.PARAMFILE = paFile;
    qc.SNAKEFILE = [  qc.name '.snQP'];
    qc.STATSFILE = [  qc.name '.stQP.csv'];

    qc.MOTILITYMAPFILE = [  qc.name '_motilityMap.maQP'];
    qc.FLUOCH1MAPFILE = [  qc.name '_fluoCh1.maQP'];
    qc.FLUOCH2MAPFILE = [  qc.name '_fluoCh2.maQP'];
    qc.FLUOCH3MAPFILE = [  qc.name '_fluoCh3.maQP'];
    qc.CONVEXMAPFILE = [ qc.name '_convexityMap.maQP'];
    qc.COORDMAPFILE = [  qc.name '_coordMap.maQP'];
    qc.ORIGINMAPFILE = [  qc.name '_originMap.maQP'];
    qc.XMAPFILE = [ qc.name '_xMap.maQP'];
    qc.YMAPFILE = [  qc.name '_yMap.maQP'];
    
    % read in data
    [qc.outlines, qc.fluo, qc.R, qc.maxSpeed, qc.outlineHeaders, qc.fluoHeaders] = readOutlines( [p qc.SNAKEFILE ]);
    if( ~isempty(qc.outlines) ),
        qc.nbFrames = length(qc.outlines);
    else,
        qc.nbFrames = 0;
    end
    
    [qc.stats, qc.fluoStats, qc.statHeaders, qc.fluoStatHeaders] = readStats( [ p qc.STATSFILE] );
    paQP = readParams([p qc.PARAMFILE ]);
    
    qc.SEGTIFF = paQP.SEGFILE;
    qc.PS = paQP.pixelSize; % pixel scale
    qc.FI = paQP.frameInterval;  % frame interval
    qc.cortexWidth = [paQP.cortexWidth, nan, nan]; 
    qc.startFrame = paQP.startFrame;
    qc.endFrame = paQP.endFrame;   
    qc.FLUOCH1TIFF =  paQP.FLUOCH1FILE;
    qc.FLUOCH2TIFF =  paQP.FLUOCH2FILE;
    qc.FLUOCH3TIFF =  paQP.FLUOCH3FILE;
    
    % fix missing params
    if( isempty(qc.startFrame) || isempty(qc.endFrame) ),
        qc.startFrame = 1;
        qc.endFrame = qc.nbFrames;
    end
    
    qc.frames = [qc.startFrame:qc.endFrame];
    
    % get cortex width from stats, if new version
    if(~isempty(qc.cortexWidth) && ~isempty(qc.fluoStats)),
        qc.cortexWidth = [qc.fluoStats(1,1,4), qc.fluoStats(1,2,4), qc.fluoStats(1,3,4)];
    end
    
    qc.motilityMap = readMap([p qc.MOTILITYMAPFILE]);
    
    qc.fluoCh1Map = readMap([p, qc.FLUOCH1MAPFILE]);
    qc.fluoCh2Map = readMap([p, qc.FLUOCH2MAPFILE]);
    qc.fluoCh3Map = readMap([p, qc.FLUOCH3MAPFILE]);
    
    if( isempty(qc.fluoCh1Map) && isempty(qc.fluoCh2Map) && isempty(qc.fluoCh3Map) ),
        % no new map types. look for old map type
        fprintf('\tRead old fluoMap.QP file');
        qc.FLUOCH1MAPFILE = [  qc.name '_fluMap.maQP'];
        qc.fluoCh1Map = readMap([p, qc.FLUOCH1MAPFILE]);    
    end
    
    qc.coordMap = readMap([p, qc.COORDMAPFILE]);
    qc.originMap = readMap([p, qc.ORIGINMAPFILE]);
    qc.convexMap = readMap([p, qc.CONVEXMAPFILE]);
    qc.xMap = readMap([p , qc.XMAPFILE]);
    qc.yMap = readMap([ p , qc.YMAPFILE]);

    
%     filter = [-1 ; 1 ];
%     if(~isempty(qc.motilityMap)),
%         qc.accMap = imfilter(qc.motilityMap, filter);  % acceleration map
%     else,
%        fprintf('Warning: Could not make accMap due to missing files\n');
%     end
%     
%     if(~isempty(qc.fluoCh1Map)),
%        qc.fluoCh1GradMap = imfilter(qc.fluoMap, filter); % fluo gradient map
%     else,
%        fprintf('Warning: Could not make fluoGradMap due to missing files\n');
%     end
    
        
    
    if(~isempty(qc.originMap) || ~isempty(qc.coordMap)),
        [qc.forwardMap,qc.backwardMap ] = buildTrackMaps(qc.originMap,qc.coordMap);
    else
        qc.forwardMap = [];
        qc.backwardMap = [];
        fprintf('Warning: Could not make tracking maps due to missing files\n');
    end
    
    
    qc.SEGTIFF = checkExists(p, qc.SEGTIFF);
    
    if(~isempty(qc.FLUOCH1TIFF)),
        qc.FLUOCH1TIFF = checkExists(p, qc.FLUOCH1TIFF);
        qc.FLUOCH2TIFF = checkExists(p, qc.FLUOCH2TIFF);
        qc.FLUOCH3TIFF = checkExists(p, qc.FLUOCH3TIFF);
    end
    
    qc.disMap = []; % placeholder for displacement map
    
    return;
    
    %----Tracking data-----
    qc.INFOFILE = [p qc.name(1:end-1) 'info.txt'];
    if(~exist(qc.INFOFILE,'file')),
        qc.INFOFILE = [p 'info.txt'];
    end
    
    qc.MANUALFILE = [p qc.name(1:end-1) 'man.csv'];
    if(isempty(qc.FLUOCH1TIFF)),
        qc.FLUOCH1TIFF = [p qc.name(1:end-1) 'act.tiff'];
    end
%     
%     if(exist(qc.INFOFILE,'file')),
%         fprintf('\tInfo file found\n');
%         qc.info = readInfo(qc.INFOFILE);
%     else
%         fprintf('\tInfo file not found\n');
%         qc.info = [];
%     end
%     
    INFILE = fopen(qc.MANUALFILE);
    if(INFILE==-1), 
        fprintf('\tNo manual file for %s\n',qc.name);
        qc.manualProts = [];
    else
        qc.manualProts = mapManual(readManual(qc.MANUALFILE), qc);
    end
    qc.autoProts = [];
    
end

function NEWFILE = checkExists(p, FILE)

% check seg and fluo channel tiffs have not moved
        if(~exist(FILE,'file')),
            [~,name,ext]= fileparts(FILE);
            NEWFILE= [p,name,ext];
            if(~exist(NEWFILE,'file')),
                NEWFILE = [];
            else
                name = [name ext];
                fprintf('\tFound %s in .paQP directory\n', name);
            end
        else
            NEWFILE = FILE;
        end


end