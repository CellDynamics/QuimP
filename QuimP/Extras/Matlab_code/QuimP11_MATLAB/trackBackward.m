function [ path ] = trackBackward( backTrack, f, p, T),
% QuimP11 function
% TRACKBACKWARD   Returns the path traveling forward in time within a map.
%   
%       [P] = TRACKBACKWARD(BACKWARDMAP, F, P, T) return a T by 2 matrix, P,
%       where P(:,1) are frames (time), and P(:,2) are membrane position
%       indexes.
%  
%       BACKWARDMAP is the map outputted by BUILDTRACKMAPS. F is the
%       starting frame. P is the starting membrane position index and T is
%       the number of frames to track backward over.
%
    path = zeros(T,2);  
    path(end,:) = [f-1, backTrack(f, p)];
    
    for t = T-1:-1:1,

        path(t,:) = [f-(T-t)-1, backTrack(f-(T-t),path(t+1,2))];
        
    end
end

