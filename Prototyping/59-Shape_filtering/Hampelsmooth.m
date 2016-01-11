function [ out ] = Hampelsmooth( in, params )
% apply Hampel filter on vector in
% params: [k sigma]

coord = in;

[coordf,j] = hampel(coord,params(1),params(2));
hh = ~any(j')';
coordf = coordf(hh,:);
if hh(1)==0
    coordf = [coord(1,:); coordf];
end
if hh(end)==0
    coordf = [coordf; coord(end,:)];
end

out = coordf;

end

