function map = readMap( varargin ),
% QuimP11 function
% READMAP    Read in a .maQP file
%
%   [MAP] = READMAP() opens a get file prompt allowing the user to select a
%   .maQP for reading.
%   
%   [MAP] = READMAP(FILEPATH) reads a .maQP file specified by FILEPATH

op = size(varargin,2);

    if(op == 0),
        [inFile,pathName] = uigetfile('*.maQP*'); fileName = [pathName,inFile]; % open dialog
    elseif(op == 1),
        fileName = varargin{1,1};
    else
        error('');
    end
    
    %display(['Read: ' fileName]);
    
    try
        map = dlmread(fileName, ',');
    catch e
        map = [];        
        fprintf('\tWarning: Map file could not be opened:\n\t%s\n',fileName);
    end
    
end