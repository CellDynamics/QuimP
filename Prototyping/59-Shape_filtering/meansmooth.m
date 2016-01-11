function out = meansmooth(in,params)
% apply running median on vector in
% params: [window iterations]

w = params(1);
iter = params(2);

xcoordf = in(:,1);
ycoordf = in(:,2);

for i=1:iter
    xcoordf = padarray(xcoordf,w,'symmetric');
    ycoordf = padarray(ycoordf,w,'symmetric');
    xcoordf = filter(ones(1,w)/w,1,xcoordf);
    ycoordf = filter(ones(1,w)/w,1,ycoordf);
    xcoordf = xcoordf(w+1:length(in(:,1))+w);
    ycoordf = ycoordf(w+1:length(in(:,2))+w);
end

out(:,1) = xcoordf;
out(:,2) = ycoordf;