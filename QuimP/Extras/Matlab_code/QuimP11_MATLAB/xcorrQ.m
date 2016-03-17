function c = xcorrQ( a, b)
% QuimP11 function
% XCORRQ Perform cross/auto correlation between .maPQ maps
%
%   [C] = XCORRQ( A , B) calculates the cross correlation between A and B,
%   using the normxcorr2 function. C is a matrix the size of A containing
%   correlation coefficients (values between -1 and 1).
%   A acts as the kernel, and so B is expanded using mirroring to
%   accommodate a complete correlation.

    rs = (size(a).*2) -1; % required size of b for full convolution
    d = rs - size(b);   % difference between b's size and required size
    
    d(d<0) = 0; % dont make smaller, i.e. no negatives
    d = ceil(d./2); % amount to expand either side, rounded up
    
    b = padarray(b,d,'symmetric'); % B is expanded by d around the edge using mirror pixels
   
    %c = conv2(a, rot90(conj(b),2), 'same'); % do the convolution. Return result same size as 'a'
                                            % dont know why you need to
                                            % rotate? coppied from xconv2
                                            
    %c = conv2(a, conj(b), 'same'); % no rotation
                                            
    c = normxcorr2(a,b); %  using the normxcorr2 function                             
    
    cols = [1: size(a,2)] + floor(size(b,2)/2); % extract centre portion
                                                % ie. size of a
    rows = [1: size(a,1)] + floor(size(b,1)/2);
    
    c = c(rows, cols);

end