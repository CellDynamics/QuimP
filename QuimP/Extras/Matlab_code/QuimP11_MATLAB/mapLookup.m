function [ values ] = mapLookup(map, F, I )
% QuimP11 Function
% MAPLOOKUP   Extract values from a map according to specified coordinates
    %
    %   	V = MAPLOOKUP(M,F,I) extracts values from the map M at the frames
    %   	specified by F and indexes specified by I, where M is a QuimP
    %   	map (2D matrix).
    %
    %       F and I can be thought of as defining a window/region on the 
    %       map that can shift position along the cell outline at each frame.
    %
    %       F is a row vector of frames.  I is a 2D matrix that contains the 
    %       indexes along the cell perimeter from which to extract values.
    %       'I' should be a of size length(F) x N, where N is the number of 
    %       indexes to extract from at each frame. 
    %       
    %       Example:
    %           >>map = round(rand(6,5).*100); % construct an artificial map,
    %                                        % 6 frames in length
    %           map =
    %
    %               71    69    77    71    12
    %                3    32    80    75    50
    %               28    95    19    28    96
    %                5     3    49    68    34
    %               10    44    45    66    59
    %               82    38    65    16    22
    %
    %
    %           >>F = [2,3,4];               % frames to extract values from
    %
    %           >>I = [2,3 ; 3,4; 1,5];      % indexes 
    %           >>mapLookup(map, F,I)
    %
    %           ans =
    %
    %               32    80
    %               19    28
    %                5    34


    L = [F',I];
    
    % check in bounds on the map
    if( sum(sum(L<1)) > 0 || any(size(map,1)-L(:,1)<0) || any(any(size(map,2)-L(:,2:end)))<0),
        error('Coordinates exceed map bounds');
    end
    
    T = repmat(L(:,1), 1, size(L,2)-1);
    values = map( (size(map,1) .* (L(:,2:end)-1)) + T );


end


