function [ nodeMap] = buildNodeMap( outlines, coordMap )
%BUILDNODEMAP Summary of this function goes here
%   Detailed explanation goes here

    
nbFrames = length(outlines);

width = size(coordMap, 2);

nodeMap = zeros(size(coordMap));

for f = 1: nbFrames,
    
    o = outlines{f};
    
    nodeCoords = o(:,1); % node coods
    mapCoord = coordMap(f,:);
    
    
    for i = 1:width,
        
        c = mapCoord(i);
        
        d = abs(nodeCoords - c);
        
        [m,pm] = min(d);
        
        
        nodeMap( f, i) = pm;
       
    
    end

end



        
end

