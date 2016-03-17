function [ BW ] = buildPeakMap( grad, peakThresh)
%BUILDPEAKMAP Summary of this function goes here
%   Detailed explanation goes here

    oX=size(grad,2);
    grad = [grad,grad,grad];

    BW = imextendedmax(grad,peakThresh); % find peak regions
    
    BW = BW(:,oX+1:oX*2);
end

