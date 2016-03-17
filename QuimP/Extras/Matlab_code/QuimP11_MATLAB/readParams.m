function p = readParams(varargin),
% QuimP11 function
% READPARAMS  Reads data from '.paQP' file
%
    %   [P] = READPARAMS() prompts for a '.paQP' file and reads parameters
    %   into a structure P.
    %
    %   [P] = READPARAMS(FILEPATH) reads parameters from the file specified
    %   by FILEPATH

    op = size(varargin,2);
    if(op == 0),
        [fileName, pathname]  = uigetfile('*.paQP', 'Read .paQP file...'); 
        if(isequal(fileName,0) || isequal(pathname,0)), 
            return; 
        end;
        fileName = [pathname, fileName]
    elseif(op == 1),
        fileName = varargin{1,1};
    else,
        error('Too many arguments');
    end
    
    INFILE = fopen(fileName); %open the file
    if(INFILE==-1), 
        fprintf('Failed to read .paQP file %s.\n',fileName); 
        p = [];
        return;
    end;

    
    nbP = 0; % count the number of params
    while (feof(INFILE)~=1), 
        line = fgetl(INFILE);
        if (line(1) ~= '#'),    % skip comment lines
            nbP = nbP+1;
        end
    end
   
    INFILE=fclose(INFILE);
   
    INFILE = fopen(fileName);
    
    line = fgetl(INFILE);
    if(~isequal(line(1:3), '#p2')),
            fprintf('\t!Old QuimP10 param file...some fields will be blank\n');
            newFormat = false;
    else
            newFormat = true;
    end
        
    params = cell(nbP,1);
    f=1;
    while (feof(INFILE)~=1), % read all params into cell as string
        line = fgetl(INFILE);
        if (line(1) ~= '#'),
            params{f} = line;
            f = f+1;
        end  
    end
    
    INFILE=fclose(INFILE);
    
    p.SEGFILE = params{2}; 
    p.pixelSize = sscanf(params{4},'%f',1);
    p.frameInterval = sscanf(params{5},'%f',1);
    
    if( newFormat),
        p.cortexWidth = sscanf(params{19},'%f',1);
        p.cortexWidth = [p.cortexWidth, 0,0];
        p.startFrame = sscanf(params{20},'%f',1);
        p.endFrame = sscanf(params{21},'%f',1);
    
    
        p.FLUOCH1FILE = params{24};
        p.FLUOCH2FILE = params{25};
        p.FLUOCH3FILE = params{26};
    else

        p.cortexWidth = [];
        p.cortexWidth = [];
        p.startFrame = [];
        p.endFrame = [];
    
    
        p.FLUOCH1FILE = [];
        p.FLUOCH2FILE = [];
        p.FLUOCH3FILE = [];
    end
end