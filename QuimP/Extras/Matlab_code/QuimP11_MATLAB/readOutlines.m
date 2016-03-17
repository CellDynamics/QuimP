function [snake,snakeFluo, R,maxSpeeds, sHeaders, fHeaders] = readOutlines(varargin)
% QuimP11 function
% READSNAKE Reads data from '.snQP' files 
%
%    [OUTLINES, FLUO, R,MAXMIGRATIONS] = READSNAKE() opens a getfile dialog
%    allowing the user to select a '.snQP' file. OUTLINES is a cell array, 
%    each cell containing the data for a single frame.  The columns are as 
%    follows:
%    [Position, x-coord, y-coord, origin, global origin, speed]
%    
%    'Position' is the normalised distance from node zero.  A position of
%    0.5 refers to a node being half way around the outline.  The 'origin'
%    indicates the position from which a node originated on the previouse
%    frame.  The global origin indicates the position a node originated
%    from in the first frame.
%   
%    FLUO holds the 3 channels of fluoresence data for each node and is
%    again a cell array where each cell holds the data for one frame.  Each
%    cell holds a 3D matrix of size [num.NODES by 3 by 3] with the 
%    demensions being:
%    [NODE, CHANNEL, DATA], 
%    where FLUO(:,:,1) are the intensity values, FLUO(:,:,2) are the 
%    x-coordinates of the sample point, and FLUO(:,:,3) are the 
%    y-coordinates of the sample points.   
%
%
%    R is a 4 element vector describing a bounding box around a cells 
%    movement, R=[xmin,xmax,ymin,ymax]. When plotting the axis can be set
%    by AXIS(R).
%
%    MAXSPEEDS is a vector of length num.FRAMES containing the maximum 
%    speed of node migration at each frame.
%    
%    [SNAKE,R,MAXMIGRATIONS] = READSNAKE(FILEPATH) reads a .snQP file at 
%    the file path specified by FILEPATH

op = size(varargin,2);

    if(op == 0),
        [inFile,pathName] = uigetfile('*.snQP*'); fileName = [pathName,inFile]; % open dialog
    elseif(op == 1),
        fileName = varargin{1,1};
    end
    
additional = 0; % space around the cell for calculating R
sHeaders = {'1.Position', '2.X-coord', '3.Y-coord', '4.Origin', '5.Global origin', '6.Speed'};
fHeaders = {'1.Intensity', '2.X-coord', '3.Y-coord'};

INFILE = fopen(fileName);
if(INFILE==-1), 
    fprintf('Warning: Failed to read .snQP file %s.\n',fileName); 
    snake = []; snakeFluo = []; R = []; maxMigrations = [];
    return; 
end;

snake=cell(1,1);
snakeFluo = cell(1,1);

maxSpeeds = [];

xMax =0;
yMax =0;
yMin =10000;
xMin =10000;

stackNum = 0;
while (feof(INFILE)~=1),
  line = fgetl(INFILE);
  if(line(1)=='#') continue; end
  numNodes = sscanf(line, '%d', 1);
  
  if(~size(numNodes)), break;end;
  stackNum = stackNum + 1;
  %fprintf('Num Nodes: %d\n',numNodes); DEBUG
  
  nodes = zeros(numNodes,6); % node data
  nodesFluo = zeros(numNodes,3,3); % fluo data associated with a node
  blank = zeros(3,1);
  
  i=1;
  while(i <= numNodes),
    line = fgetl(INFILE);
    if(line(1)=='#') continue; end
    read = sscanf(line, ['%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '\f' '\t' '%f' '\t' '%f' '\t' '%f' '\t' '\f'], 15);
    nodes(i,:) = read(1:6)';
    
    if(length(read) == 7),
        % old format
        nodesFluo(i,1,:) = [read(7),-1,-1];
        nodesFluo(i,2,:) = blank;
        nodesFluo(i,3,:) = blank;
    else
        % new format
        nodesFluo(i,1,:) = read(7:9)';
        nodesFluo(i,2,:) = read(10:12)';
        nodesFluo(i,3,:) = read(13:15)';
    end
    
    x = nodes(i,2);
    y = nodes(i,3);

    
    if(x<xMin), xMin =x; end;
    if(x>xMax), xMax =x; end;
    if(y<yMin), yMin =y; end;
    if(y>yMax), yMax =y; end;
    
    %fprintf('\t %d Node values: x=%f, y=%f\n',i,x,y); DEBUG
    
    i = i+1;
  end;
  
  maxSpeeds(stackNum) = max(abs(nodes(:,6)));
  snake(stackNum,1) = {nodes};
  snakeFluo(stackNum,1) = {nodesFluo};
  
end;

fclose(INFILE);

xMin = floor(xMin - additional);
xMax = ceil(xMax + additional);

yMin = round(yMin - additional);
yMax = ceil(yMax + additional);

R = [xMin,xMax,yMin,yMax];

end





















