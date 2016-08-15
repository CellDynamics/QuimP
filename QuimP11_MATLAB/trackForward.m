function [ path ] = trackForward( forwTrack, f, p, T)
% QuimP11 function
% TRACKFORWARD   Returns the path traveling forward in time within a map.
%   
%       [P] = TRACKFORWARD(FORWARDMAP, F, A, T) returns a T by 2 matrix, P,
%       where P(:,1) are frames (time), and P(:,2) are membrane position
%       indexes.
%       f - start frame
%       p - membrane points indexes
%       T - how many frames track in
%  
%       FORWARDMAP is the map outputted by BUILDTRACKMAPS. F is the
%       starting frame. A is the starting membrane position index and T is
%       the number of frames to track forward over.
%  
    path = zeros(T,2);  
    path(1,:) = [f+1, forwTrack(f, p)];
    
    for t = 2:T,
       
        path(t,:) = [f+t, forwTrack(f+t-1,path(t-1,2))];
     
    end

end

